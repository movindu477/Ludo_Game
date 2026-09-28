package lk.apiit.ludot.selector;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;

/* CHAIN LINK - Rules 2 and 3, bring a piece out of the base on a six.

   Yellow and Green put this first because both want an empty base. Red
   puts it late, because it only releases a piece when it has nothing on
   the board to attack with. */
public class EnterBoardSelector extends MoveSelector {

    private final boolean onlyWhenBoardIsEmpty;

    public EnterBoardSelector(Board board, CommandFactory factory,
                              boolean onlyWhenBoardIsEmpty) {
        super(board, factory);
        this.onlyWhenBoardIsEmpty = onlyWhenBoardIsEmpty;
    }

    @Override
    public GameCommand select(Player player, int roll) {
        if (roll != GameRules.ROLL_TO_LEAVE_BASE) {
            return passOn(player, roll);
        }
        if (onlyWhenBoardIsEmpty && player.piecesOnBoard() > 0) {
            return passOn(player, roll);    // Red's preference
        }
        for (Piece piece : player.pieces()) {
            if (piece.isInBase()) {
                return factory.enterBoard(piece, player);
            }
        }
        return passOn(player, roll);
    }
}
