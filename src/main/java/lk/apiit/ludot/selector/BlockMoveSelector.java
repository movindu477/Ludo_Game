package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;

import java.util.List;

/* CHAIN LINK - Rule T-4, move a block as a single unit.

   Without this link MoveBlockCommand is never built, so blocks would form
   and then behave like ordinary pieces.

   The roll must divide by the block size for the block to move at all, so
   a four piece block rolling a three is skipped and the chain carries on.
   A block is also skipped when one of its members is held in a briefing
   (Rule T-13) or an opponent block stands on its route (Rule T-3). */
public class BlockMoveSelector extends MoveSelector {

    private static final int BLOCK_SIZE = 2;

    public BlockMoveSelector(Board board, CommandFactory factory) {
        super(board, factory);
    }

    @Override
    public GameCommand select(Player player, int roll) {
        for (int index = 0; index < GameRules.STANDARD_CELLS; index++) {
            List<Piece> ours = board.piecesAt(index).stream()
                    .filter(piece -> piece.colour() == player.colour())
                    .toList();

            if (ours.size() >= BLOCK_SIZE && canTravel(ours, index, roll)) {
                return factory.moveBlock(ours, roll);
            }
        }
        return passOn(player, roll);
    }

    private boolean canTravel(List<Piece> block, int index, int roll) {
        int steps = roll / block.size();
        if (steps <= 0) {
            return false;
        }
        for (Piece piece : block) {
            if (!piece.canMove()) {
                return false;                       // Rule T-13
            }
        }
        Piece leader = board.furthestFromHome(block);
        return !board.routeIsBlocked(index, leader.direction(), steps,
                leader.colour());                   // Rule T-3
    }
}
