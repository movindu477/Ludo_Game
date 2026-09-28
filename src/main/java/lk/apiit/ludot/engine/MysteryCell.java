package lk.apiit.ludot.engine;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.effect.EffectFactory;

import java.util.List;
import java.util.Random;

/* Rules T-10 and T-11 - the mystery cell's location and what it does.

   It appears after two rounds with pieces on the path, stays four rounds,
   then moves somewhere else, and never to the square it just left. */
public class MysteryCell {

    private final Random random = new Random();

    private Integer location;              // null until it first appears
    private int roundsRemaining;
    private int roundsWithPiecesOnPath;

    public Integer location() {
        return location;
    }

    public int roundsLeft() {
        return roundsRemaining;
    }

    public boolean isAt(int index) {
        return location != null && location == index;
    }

    /* Called at the end of every round. Returns true when the cell has
       just moved, so the caller can print the spawn message. */
    public boolean onRoundEnd(Board board) {
        if (location == null) {
            if (board.pieceCountOnPath() > 0) {
                roundsWithPiecesOnPath++;
            }
            return roundsWithPiecesOnPath >= GameRules.ROUNDS_BEFORE_MYSTERY
                    && relocate(board);
        }
        roundsRemaining--;
        return roundsRemaining <= 0 && relocate(board);
    }

    private boolean relocate(Board board) {
        List<Integer> free = board.emptyPathCells();
        Integer previous = location;
        free.removeIf(index -> index.equals(previous));   // Rule T-10

        if (free.isEmpty()) {
            return false;                  // nowhere to go, try next round
        }
        location = free.get(random.nextInt(free.size()));
        roundsRemaining = GameRules.MYSTERY_LIFETIME_ROUNDS;
        return true;
    }

    /* Rule T-11: teleport the piece that landed here, and Rule T-14 for
       the Gamma special case - a counterclockwise piece goes to Beta
       instead, and a clockwise one turns around. */
    public void teleport(Piece piece, Board board, EffectFactory effects, ConsoleView view) {
        EffectFactory.Destination destination = effects.nextDestination();

        if (destination == EffectFactory.Destination.GAMMA) {
            if (piece.direction() == Direction.COUNTER_CLOCKWISE) {
                destination = EffectFactory.Destination.BETA;
                view.showGammaToBeta(piece);
            } else {
                piece.turnAround();
                view.showDirectionChanged(piece);
            }
        }

        board.lift(piece);
        if (destination == EffectFactory.Destination.BASE) {
            piece.sendToBase();
        } else {
            board.placeOnPath(piece, indexOf(destination, piece.colour(), board));
        }

        piece.applyEffect(effects.effectFor(destination));
        view.showTeleport(piece, destination.name());
    }

    /* BASE never reaches here - teleport() handles it before placing. */
    private int indexOf(EffectFactory.Destination destination, Colour colour, Board board) {
        int yellowApproach = board.approachIndexOf(Colour.YELLOW);
        return switch (destination) {
            case ALPHA -> board.step(yellowApproach, Direction.CLOCKWISE, GameRules.ALPHA_OFFSET);
            case BETA -> board.step(yellowApproach, Direction.CLOCKWISE, GameRules.BETA_OFFSET);
            case GAMMA -> board.step(yellowApproach, Direction.CLOCKWISE, GameRules.GAMMA_OFFSET);
            case START_X, BASE -> board.startIndexOf(colour);
            case APPROACH -> board.approachIndexOf(colour);
        };
    }
}
