package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;

import java.util.List;

/* CHAIN LINK - the ordinary move, and the last link that can actually
   produce one.

   The "preference" decides which piece is chosen when several could move.
   Yellow advances whichever is nearest home; Blue cycles B1 to B4 in turn.
   Passing the preference in avoids four nearly identical selector classes. */
public class AdvanceSelector extends MoveSelector {

    public enum Preference { NEAREST_HOME, FURTHEST_FROM_HOME, IN_TURN }

    private final Preference preference;
    private int cursor;                    // only used by IN_TURN

    public AdvanceSelector(Board board, CommandFactory factory, Preference preference) {
        super(board, factory);
        this.preference = preference;
    }

    @Override
    public GameCommand select(Player player, int roll) {
        List<Piece> movable = movablePieces(player, roll);
        if (movable.isEmpty()) {
            return passOn(player, roll);
        }
        Piece chosen = switch (preference) {
            case NEAREST_HOME -> byJourney(movable, true);
            case FURTHEST_FROM_HOME -> byJourney(movable, false);
            case IN_TURN -> nextInCycle(movable);
        };
        return factory.movePiece(chosen, player, chosen.stepsFor(roll));
    }

    private Piece byJourney(List<Piece> movable, boolean nearest) {
        Piece best = movable.get(0);
        for (Piece piece : movable) {
            boolean better = nearest
                    ? journey(piece) < journey(best)
                    : journey(piece) > journey(best);
            if (better) {
                best = piece;
            }
        }
        return best;
    }

    /* Section 2.1.4: Blue takes its pieces strictly in turn. */
    private Piece nextInCycle(List<Piece> movable) {
        Piece chosen = movable.get(cursor % movable.size());
        cursor++;
        return chosen;
    }

    /* Cells left before the home straight, in the piece's own direction
       (Rule T-1), so a counterclockwise piece is measured correctly. */
    private int journey(Piece piece) {
        if (piece.position().isOnHomeStraight()) {
            return 0;
        }
        return board.stepsToApproach(piece);
    }
}
