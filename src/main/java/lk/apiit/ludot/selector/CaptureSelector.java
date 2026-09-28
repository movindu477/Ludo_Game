package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;

import java.util.List;

/* CHAIN LINK - Rule 6, take a capture if one is available.

   When several captures are possible it takes the opponent piece closest
   to its OWN home, which is the biggest setback for that player. Red puts
   this link first; Yellow puts it second. */
public class CaptureSelector extends MoveSelector {

    public CaptureSelector(Board board, CommandFactory factory) {
        super(board, factory);
    }

    @Override
    public GameCommand select(Player player, int roll) {
        Piece best = null;
        int bestSteps = 0;
        int shortestVictimJourney = Integer.MAX_VALUE;

        for (Piece piece : movablePieces(player, roll)) {
            int steps = piece.stepsFor(roll);
            Position target = board.targetOf(piece, steps);

            if (target == null || !target.isOnPath()) {
                continue;
            }
            List<Piece> victims = board.opponentsAt(target.index(), player.colour());
            if (victims.isEmpty()) {
                continue;
            }
            // the victim's remaining journey, in its own direction (Rule T-1)
            int journey = board.stepsToApproach(victims.get(0));
            if (journey < shortestVictimJourney) {
                shortestVictimJourney = journey;
                best = piece;
                bestSteps = steps;
            }
        }
        return best == null
                ? passOn(player, roll)
                : factory.movePiece(best, player, bestSteps);
    }
}
