package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;

/* CHAIN LINK - Rule T-3, prefer a move that lands on one of our own pieces
   and so forms a block.

   Only Green uses this link, because blocking is Green's whole strategy
   (Section 2.1.2). The other three chains leave it out entirely - which is
   the advantage of letting each strategy build its own chain. */
public class BlockFormingSelector extends MoveSelector {

    public BlockFormingSelector(Board board, CommandFactory factory) {
        super(board, factory);
    }

    @Override
    public GameCommand select(Player player, int roll) {
        for (Piece piece : movablePieces(player, roll)) {
            int steps = piece.stepsFor(roll);
            Position target = board.targetOf(piece, steps);

            if (target == null || !target.isOnPath()) {
                continue;
            }
            if (ourOwnPieceIsAt(target.index(), piece)) {
                return factory.movePiece(piece, player, steps);
            }
        }
        return passOn(player, roll);
    }

    /* Another piece of our colour, not the mover itself - a six-step move
       can never land back where it started, but the check keeps it honest. */
    private boolean ourOwnPieceIsAt(int index, Piece mover) {
        for (Piece occupant : board.piecesAt(index)) {
            if (occupant != mover && occupant.colour() == mover.colour()) {
                return true;
            }
        }
        return false;
    }
}
