package lk.apiit.ludot.command;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.engine.ConsoleView;

import java.util.List;

/* COMMAND - Rule T-4, moving a block as a single unit.

   Two parts to the rule: the roll is divided by how many pieces are in the
   block, and a mixed-direction block travels the way the piece furthest
   from home is facing.

   Integer division is deliberate. A four-piece block rolling a three gets
   zero and does not move; rounding up would make blocks always profitable
   and unbalance the game. */
public class MoveBlockCommand implements GameCommand {

    private final List<Piece> members;
    private final int roll;
    private final Board board;
    private final ConsoleView view;

    public MoveBlockCommand(List<Piece> members, int roll, Board board, ConsoleView view) {
        this.members = List.copyOf(members);
        this.roll = roll;
        this.board = board;
        this.view = view;
    }

    @Override
    public void execute() {
        int steps = roll / members.size();
        if (steps <= 0) {
            return;                         // the roll does not divide, no move
        }
        Direction direction = board.furthestFromHome(members).direction();
        int from = members.get(0).position().index();
        int to = board.step(from, direction, steps);

        for (Piece piece : members) {
            notePassIfTravellingOwnWay(piece, direction, steps);
            board.lift(piece);
            board.placeOnPath(piece, to);   // they stay together
        }
        view.showBlockMove(members.get(0).colour(), members.size(), from, to, steps, direction);
    }

    /* Rule T-1: a member only counts an approach pass when the block is
       carrying it the way it normally travels - measured before it moves. */
    private void notePassIfTravellingOwnWay(Piece piece, Direction direction, int steps) {
        if (piece.direction() == direction && board.passesApproach(piece, steps)) {
            piece.notePassedApproach();
        }
    }
}
