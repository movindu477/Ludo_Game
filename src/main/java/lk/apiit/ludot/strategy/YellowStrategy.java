package lk.apiit.ludot.strategy;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.selector.AdvanceSelector;
import lk.apiit.ludot.selector.BlockMoveSelector;
import lk.apiit.ludot.selector.CaptureSelector;
import lk.apiit.ludot.selector.EnterBoardSelector;
import lk.apiit.ludot.selector.MoveSelector;

/* STRATEGY - Section 2.1.3, the player that just wants to finish.

   Base first, then a capture if one is going spare (it still needs one per
   piece for Rule T-7), then simply push whichever piece is nearest home. */
public class YellowStrategy implements PlayerStrategy {

    @Override
    public MoveSelector buildChain(Board board, CommandFactory factory) {
        MoveSelector head = new EnterBoardSelector(board, factory, false);
        head.linkTo(new CaptureSelector(board, factory))
            .linkTo(new BlockMoveSelector(board, factory))          // Rule T-4
            .linkTo(new AdvanceSelector(board, factory,
                    AdvanceSelector.Preference.NEAREST_HOME));
        return head;
    }

    @Override
    public String name() {
        return "home-seeking";
    }
}
