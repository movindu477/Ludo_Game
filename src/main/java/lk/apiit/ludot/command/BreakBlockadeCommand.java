package lk.apiit.ludot.command;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.engine.ConsoleView;

import java.util.List;

/* COMMAND - Rules T-5 and T-6.

   Three sixes in a row forces a player holding a blockade to break it.
   "Six units cumulatively" is read as six shared between the pieces that
   move, not six each, and one piece stays behind - that reading penalises
   the blockade rather than rewarding the forfeited turn.

   Rule T-5: each freed piece goes back to the direction it had when it
   first left the base.

   This is also what stops the game deadlocking. Blocks that move as units
   bunch together, and Rule T-3 then makes every move illegal for everyone.
   T-6 is the game's own way out. */
public class BreakBlockadeCommand implements GameCommand {

    private final List<Piece> members;
    private final Board board;
    private final ConsoleView view;

    public BreakBlockadeCommand(List<Piece> members, Board board, ConsoleView view) {
        this.members = List.copyOf(members);
        this.board = board;
        this.view = view;
    }

    @Override
    public void execute() {
        int moving = members.size() - 1;              // one piece stays put
        if (moving <= 0) {
            return;
        }
        int each = GameRules.DICE_FACES / moving;     // the six, shared out

        for (int i = 0; i < moving; i++) {
            Piece piece = members.get(i);
            piece.restoreOriginalDirection();         // Rule T-5
            if (board.passesApproach(piece, each)) {
                piece.notePassedApproach();           // Rule T-1, as for any move
            }
            int to = board.step(piece.position().index(), piece.direction(), each);
            board.lift(piece);
            board.placeOnPath(piece, to);
        }
        view.showBlockadeBroken(members.get(0).colour());
    }
}
