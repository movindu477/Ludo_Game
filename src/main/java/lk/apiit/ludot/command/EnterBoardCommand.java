package lk.apiit.ludot.command;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.engine.ConsoleView;

/* COMMAND - Rule 2 plus Rule T-1.

   Moves a piece from the base onto its X square, and the coin toss fixes
   the direction it will travel for the rest of the game. */
public class EnterBoardCommand implements GameCommand {

    private final Piece piece;
    private final Player player;
    private final Board board;
    private final Direction tossed;
    private final ConsoleView view;

    public EnterBoardCommand(Piece piece, Player player, Board board,
                             Direction tossed, ConsoleView view) {
        this.piece = piece;
        this.player = player;
        this.board = board;
        this.tossed = tossed;
        this.view = view;
    }

    @Override
    public void execute() {
        int start = board.startIndexOf(piece.colour());
        piece.enterBoard(start, tossed);   // stores BOTH directions, Rule T-5
        board.placeOnPath(piece, start);
        view.showEnteredBoard(player, piece);
    }
}
