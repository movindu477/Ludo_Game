package lk.apiit.ludot.strategy;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.selector.AdvanceSelector;
import lk.apiit.ludot.selector.BlockMoveSelector;
import lk.apiit.ludot.selector.CaptureSelector;
import lk.apiit.ludot.selector.EnterBoardSelector;
import lk.apiit.ludot.selector.MoveSelector;

/* STRATEGY - Section 2.1.1, the aggressive player.

   Capture comes first. Red only brings another piece out when it has
   nothing on the board at all, which is why the EnterBoardSelector is
   built with onlyWhenBoardIsEmpty set to true. */
public class RedStrategy implements PlayerStrategy {

    @Override
    public MoveSelector buildChain(Board board, CommandFactory factory) {
        MoveSelector head = new CaptureSelector(board, factory);
        head.linkTo(new EnterBoardSelector(board, factory, true))
            .linkTo(new BlockMoveSelector(board, factory))          // Rule T-4
            .linkTo(new AdvanceSelector(board, factory,
                    AdvanceSelector.Preference.FURTHEST_FROM_HOME));
        return head;
    }

    @Override
    public String name() {
        return "aggressive";
    }
}
