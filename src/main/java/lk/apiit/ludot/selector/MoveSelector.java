package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;

import java.util.ArrayList;
import java.util.List;

/* CHAIN OF RESPONSIBILITY - the base link.

   Each link tries to produce one kind of move. If it cannot, it hands the
   request to the next link. The chain ends in a Null Object, so select()
   can never return null.

   The ORDER of the chain is itself the decision, which is why each player
   strategy builds its own order rather than sharing one fixed chain. */
public abstract class MoveSelector {

    protected final Board board;
    protected final CommandFactory factory;
    private MoveSelector next;

    protected MoveSelector(Board board, CommandFactory factory) {
        this.board = board;
        this.factory = factory;
    }

    /* Returns the link just added, so a chain reads as one statement:
       a.linkTo(b).linkTo(c) */
    public MoveSelector linkTo(MoveSelector next) {
        this.next = next;
        return next;
    }

    public abstract GameCommand select(Player player, int roll);

    /* NULL OBJECT: the end of the chain is a command that does nothing,
       not a null that every caller would have to check. */
    protected GameCommand passOn(Player player, int roll) {
        return next == null ? factory.noMove() : next.select(player, roll);
    }

    /* Shared by several links: the pieces that could legally move this
       roll, with blocked routes and briefing-held pieces removed. */
    protected List<Piece> movablePieces(Player player, int roll) {
        List<Piece> movable = new ArrayList<>();
        for (Piece piece : player.pieces()) {
            if (piece.isInBase() || piece.isHome() || !piece.canMove()) {
                continue;
            }
            int steps = piece.stepsFor(roll);
            if (steps <= 0 || board.pathIsBlocked(piece, steps)) {
                continue;                          // Rule T-3
            }
            if (board.targetOf(piece, steps) != null) {
                movable.add(piece);
            }
        }
        return movable;
    }
}
