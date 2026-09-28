package lk.apiit.ludot;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.MysteryCell;
import lk.apiit.ludot.strategy.RedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandTest {

    private Board board;
    private CommandFactory factory;

    @BeforeEach
    void setUp() {
        board = new Board();
        factory = new CommandFactory(board, new MysteryCell(),
                new EffectFactory(), new ConsoleView());
    }

    @Test
    void capturingSendsTheOpponentBackToBaseAndEarnsAnotherRoll() {
        // Rules 6, T-2 and T-9
        Player red = new Player(Colour.RED, new RedStrategy());
        Piece attacker = red.pieces().get(0);
        Piece victim = new Piece(Colour.GREEN, 1);

        factory.enterBoard(attacker, red).execute();
        int landing = board.step(attacker.position().index(), attacker.direction(), 3);
        board.placeOnPath(victim, landing);

        GameCommand move = factory.movePiece(attacker, red, 3);
        move.execute();

        assertThat(victim.isInBase()).isTrue();
        assertThat(victim.hasCaptured()).isFalse();   // Rule T-9 resets it
        assertThat(attacker.hasCaptured()).isTrue();  // Rule T-7 needs this
        assertThat(move.grantsExtraRoll()).isTrue();  // Rule T-2
    }

    @Test
    void threeSixesBreakABlockadeAndRestoreOriginalDirections() {
        // Rules T-5 and T-6
        Player red = new Player(Colour.RED, new RedStrategy());
        Piece first = red.pieces().get(0);
        Piece second = red.pieces().get(1);

        first.enterBoard(20, Direction.CLOCKWISE);
        second.enterBoard(20, Direction.CLOCKWISE);
        board.placeOnPath(first, 20);
        board.placeOnPath(second, 20);
        assertThat(board.blockadeOwnedBy(Colour.RED)).hasSize(2);

        first.turnAround();                   // Rule T-14 flipped it on the way
        factory.breakBlockade(board.blockadeOwnedBy(Colour.RED)).execute();

        assertThat(board.blockadeOwnedBy(Colour.RED)).isEmpty();   // broken up
        assertThat(first.direction()).isEqualTo(Direction.CLOCKWISE);  // Rule T-5
        assertThat(first.position()).isEqualTo(Position.onPath(26));   // six, one piece moving
        assertThat(second.position()).isEqualTo(Position.onPath(20));  // one stays behind
    }

    @Test
    void theNullCommandDoesNothingAndHarmsNothing() {
        Player red = new Player(Colour.RED, new RedStrategy());

        factory.noMove().execute();      // no exception, no change

        assertThat(red.piecesInBase()).isEqualTo(4);
    }
}
