package lk.apiit.ludot.strategy;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.selector.AdvanceSelector;
import lk.apiit.ludot.selector.BlockFormingSelector;
import lk.apiit.ludot.selector.BlockMoveSelector;
import lk.apiit.ludot.selector.EnterBoardSelector;
import lk.apiit.ludot.selector.MoveSelector;

/* STRATEGY - Section 2.1.2, the blocking player.

   Green wants an empty base, but a move that creates a block comes first,
   because blocking opponents is the whole point of this behaviour. It is
   the only strategy that uses BlockFormingSelector. */
public class GreenStrategy implements PlayerStrategy {

    @Override
    public MoveSelector buildChain(Board board, CommandFactory factory) {
        MoveSelector head = new BlockFormingSelector(board, factory);
        head.linkTo(new EnterBoardSelector(board, factory, false))
            .linkTo(new BlockMoveSelector(board, factory))          // Rule T-4
            .linkTo(new AdvanceSelector(board, factory,
                    AdvanceSelector.Preference.FURTHEST_FROM_HOME));
        return head;
    }

    @Override
    public String name() {
        return "blocking";
    }
}
