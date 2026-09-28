package lk.apiit.ludot;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
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
import lk.apiit.ludot.selector.MoveSelector;
import lk.apiit.ludot.strategy.GreenStrategy;
import lk.apiit.ludot.strategy.RedStrategy;
import lk.apiit.ludot.strategy.YellowStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorChainTest {

    private Board board;
    private CommandFactory factory;

    @BeforeEach
    void setUp() {
        board = new Board();
        factory = new CommandFactory(board, new MysteryCell(),
                new EffectFactory(), new ConsoleView());
    }

    @Test
    void theChainReturnsTheNullObjectWhenNothingCanMove() {
        // every piece is still in the base and the roll is not a six
        Player red = new Player(Colour.RED, new RedStrategy());
        MoveSelector chain = red.strategy().buildChain(board, factory);

        GameCommand chosen = chain.select(red, 4);

        assertThat(chosen).isSameAs(NullMoveCommand.INSTANCE);
        assertThat(chosen.isNoMove()).isTrue();
    }

    @Test
    void aSixReleasesAPieceFromTheBase() {
        // Rule 2 - Yellow puts EnterBoardSelector first
        Player yellow = new Player(Colour.YELLOW, new YellowStrategy());
        MoveSelector chain = yellow.strategy().buildChain(board, factory);

        GameCommand chosen = chain.select(yellow, 6);

        assertThat(chosen).isNotInstanceOf(NullMoveCommand.class);
        chosen.execute();
        assertThat(yellow.piecesInBase()).isEqualTo(3);
        assertThat(yellow.piecesOnBoard()).isEqualTo(1);
    }

    @Test
    void aBlockMovesAsOneUnitByTheRollDividedByItsSize() {
        // Rule T-4 - a two piece block rolling a four moves two cells together
        Player green = new Player(Colour.GREEN, new GreenStrategy());
        Piece g1 = green.pieces().get(0);
        Piece g2 = green.pieces().get(1);
        for (Piece piece : List.of(g1, g2)) {
            piece.enterBoard(10, Direction.CLOCKWISE);
            board.placeOnPath(piece, 10);
        }
        MoveSelector chain = green.strategy().buildChain(board, factory);

        chain.select(green, 4).execute();

        assertThat(g1.position()).isEqualTo(Position.onPath(12));
        assertThat(g2.position()).isEqualTo(Position.onPath(12));
        assertThat(board.hasBlockAt(12)).isTrue();
    }

    @Test
    void redHoldsItsPiecesBackWhileOneIsAlreadyOnTheBoard() {
        // Section 2.1.1 - Red only releases when the board is empty
        Player red = new Player(Colour.RED, new RedStrategy());
        MoveSelector chain = red.strategy().buildChain(board, factory);

        chain.select(red, 6).execute();      // first piece comes out
        chain.select(red, 6).execute();      // second six

        assertThat(red.piecesOnBoard()).isEqualTo(1);
    }
}
