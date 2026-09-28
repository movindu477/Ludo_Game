package lk.apiit.ludot.strategy;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.selector.AdvanceSelector;
import lk.apiit.ludot.selector.BlockMoveSelector;
import lk.apiit.ludot.selector.EnterBoardSelector;
import lk.apiit.ludot.selector.MoveSelector;

/* STRATEGY - Section 2.1.4, the player with no plan.

   Blue takes its pieces strictly in turn, B1 to B4 and round again, so the
   AdvanceSelector uses the IN_TURN preference. Only a block that can move
   as one unit (Rule T-4) is taken before it. The chain is
   built once per player and kept, because that selector has to remember
   where it is in the cycle between turns. */
public class BlueStrategy implements PlayerStrategy {

    @Override
    public MoveSelector buildChain(Board board, CommandFactory factory) {
        MoveSelector head = new BlockMoveSelector(board, factory); // Rule T-4
        head.linkTo(new AdvanceSelector(board, factory,
                    AdvanceSelector.Preference.IN_TURN))
            .linkTo(new EnterBoardSelector(board, factory, false));
        return head;
    }

    @Override
    public String name() {
        return "random";
    }
}
