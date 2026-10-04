package lk.apiit.ludot;

import lk.apiit.ludot.command.BreakBlockadeCommand;
import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.EnterBoardCommand;
import lk.apiit.ludot.command.MoveBlockCommand;
import lk.apiit.ludot.command.MovePieceCommand;
import lk.apiit.ludot.command.NullMoveCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.MysteryCell;
import lk.apiit.ludot.strategy.PlayerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandFactoryTest {

    private Board board;
    private CommandFactory factory;
    private Player player;
    private Piece piece;

    @BeforeEach
    void setUp() {
        board = new Board();
        factory = new CommandFactory(board, new MysteryCell(),
                new EffectFactory(), new ConsoleView());
        player = new PlayerFactory().create(Colour.YELLOW);
        piece = player.pieces().get(0);
    }

    /* Two yellow pieces sharing cell 20, both travelling clockwise. */
    private List<Piece> blockAt20() {
        List<Piece> members = player.pieces().subList(0, 2);
        for (Piece member : members) {
            member.enterBoard(20, Direction.CLOCKWISE);
            board.placeOnPath(member, 20);
        }
        return members;
    }

    @Test
    void enterBoardBuildsAnEnterCommand() {
        assertThat(factory.enterBoard(piece, player)).isInstanceOf(EnterBoardCommand.class);
    }

    @Test
    void movePieceBuildsAMoveCommand() {
        assertThat(factory.movePiece(piece, player, 3)).isInstanceOf(MovePieceCommand.class);
    }

    @Test
    void moveBlockBuildsABlockCommand() {
        assertThat(factory.moveBlock(List.of(piece), 4)).isInstanceOf(MoveBlockCommand.class);
    }

    @Test
    void breakBlockadeBuildsABreakCommand() {
        assertThat(factory.breakBlockade(List.of(piece))).isInstanceOf(BreakBlockadeCommand.class);
    }

    @Test
    void noMoveReturnsTheSharedNullObject() {
        assertThat(factory.noMove()).isSameAs(NullMoveCommand.INSTANCE);
    }

    @Test
    void theNullObjectIsTheSameEveryTime() {
        // SINGLETON - asking twice never builds a second one
        assertThat(factory.noMove()).isSameAs(factory.noMove());
    }

    @Test
    void theNullObjectReportsItselfAsNoMove() {
        assertThat(factory.noMove().isNoMove()).isTrue();
    }

    @Test
    void aRealCommandIsNotANoMove() {
        assertThat(factory.movePiece(piece, player, 2).isNoMove()).isFalse();
    }

    @Test
    void theNullObjectGrantsNoExtraRoll() {
        assertThat(factory.noMove().grantsExtraRoll()).isFalse();
    }

    @Test
    void executingTheNullObjectChangesNothing() {
        // a piece on the board, so "nothing changed" is worth checking
        factory.enterBoard(piece, player).execute();
        Position before = piece.position();

        factory.noMove().execute();

        assertThat(piece.position()).isEqualTo(before);
        assertThat(board.piecesAt(before.index())).containsExactly(piece);
        assertThat(player.piecesInBase()).isEqualTo(3);
    }

    @Test
    void enteringTheBoardPutsThePieceOnItsStartSquare() {
        // Rule 2 and Rule T-1 - the coin toss always fixes a direction
        factory.enterBoard(piece, player).execute();

        assertThat(piece.position()).isEqualTo(Position.onPath(board.startIndexOf(Colour.YELLOW)));
        assertThat(piece.direction()).isNotNull();
        assertThat(board.piecesAt(board.startIndexOf(Colour.YELLOW))).containsExactly(piece);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void everyRollMovesThePieceTheRolledDistance(int roll) {
        // either coin toss: clockwise heads up the board, counterclockwise
        // wraps back past 0 and carries on (no capture yet, Rule T-7)
        factory.enterBoard(piece, player).execute();
        int start = piece.position().index();

        factory.movePiece(piece, player, roll).execute();

        assertThat(piece.position())
                .isEqualTo(Position.onPath(board.step(start, piece.direction(), roll)));
        assertThat(board.isEmpty(start)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"1, 20", "2, 21", "3, 21", "4, 22", "5, 22", "6, 23"})
    void aBlockOfTwoMovesHalfTheRoll(int roll, int expectedCell) {
        // Rule T-4 - the roll is divided by the block size, rounded down
        List<Piece> block = blockAt20();

        factory.moveBlock(block, roll).execute();

        assertThat(block).allMatch(p -> p.position().equals(Position.onPath(expectedCell)));
    }
}
