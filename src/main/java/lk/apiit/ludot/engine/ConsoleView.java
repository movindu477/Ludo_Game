package lk.apiit.ludot.engine;

import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;
import lk.apiit.ludot.dto.PieceLocationDto;
import lk.apiit.ludot.dto.PlayerStatusDto;

import java.util.List;

/* Every output message from Section 3 of the brief, in one class, and the
   only class in the project that calls System.out.

   It receives DTOs rather than live domain objects for the status report,
   so it cannot accidentally change the game while printing it. */
public class ConsoleView {

    public void showPlayerPieces(Player player) {
        print("The " + player.colour().displayName()
                + " player has four (04) pieces named " + player.pieceNames() + ".");
    }

    public void showOpeningRoll(Colour colour, int value) {
        print(colour.displayName() + " rolls " + value);
    }

    public void showFirstPlayer(Colour first, List<Colour> order) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            text.append(order.get(i).displayName());
            if (i < order.size() - 2) {
                text.append(", ");
            } else if (i == order.size() - 2) {
                text.append(", and ");
            }
        }
        print(first.displayName() + " player has the highest roll and will begin the game.");
        print("The order of a single round is " + text + ".");
    }

    public void showRoll(Player player, int value) {
        print(player.colour().displayName() + " player rolled " + value + ".");
    }

    public void showEnteredBoard(Player player, Piece piece) {
        print(player.colour().displayName() + " player moves piece "
                + piece.name() + " to the starting point.");
        print(counts(player));
    }

    public void showMove(Piece piece, Position from, Position to, int units) {
        print(piece.colour().displayName() + " moves piece " + piece.name()
                + " from location " + from.label() + " to " + to.label()
                + " by " + units + " units in " + describe(piece.direction()) + " direction.");
    }

    public void showCapture(Player player, Piece capturing, Piece captured, Position at) {
        print(capturing.colour().displayName() + " piece " + capturing.name()
                + " lands on square " + at.label() + ", captures "
                + captured.colour().displayName() + " piece " + captured.name()
                + ", and returns it to the base.");
        print(counts(player));
    }

    public void showBlockMove(Colour colour, int size, int from, int to,
                              int units, Direction direction) {
        print(colour.displayName() + " moves a block of " + size
                + " pieces from location " + from + " to " + to
                + " by " + units + " units in " + describe(direction) + " direction.");
    }

    public void showNoMove(Player player) {
        print(player.colour().displayName()
                + " has no legal move. Ignoring the throw and moving on to the next player.");
    }

    public void showForfeit(Player player) {
        print(player.colour().displayName()
                + " rolled three consecutive sixes and forfeits the turn.");
    }

    public void showBlockadeBroken(Colour colour) {
        print(colour.displayName()
                + " rolled three consecutive sixes and must break the blockade.");
    }

    public void showReachedHome(Piece piece) {
        print(piece.colour().displayName() + " piece " + piece.name() + " has reached Home.");
    }

    public void showMysterySpawn(int index) {
        print("A mystery cell has spawned in location " + index
                + " and will be at this location for the next four rounds.");
    }

    public void showTeleport(Piece piece, String destination) {
        print(piece.colour().displayName() + " piece " + piece.name()
                + " teleported to " + destination + " - now " + piece.effect().describe() + ".");
    }

    public void showDirectionChanged(Piece piece) {
        print("The " + piece.colour().displayName() + " piece " + piece.name()
                + ", which was moving clockwise, has changed to moving counterclockwise.");
    }

    public void showGammaToBeta(Piece piece) {
        print("The " + piece.colour().displayName() + " piece " + piece.name()
                + " is moving counterclockwise. Teleporting to Beta from Gamma.");
    }

    public void showBriefingEscape(Piece piece) {
        print(piece.colour().displayName() + " piece " + piece.name()
                + " rolled three consecutively. Teleporting piece to base.");
    }

    /* Takes DTOs, not Players - the view gets a snapshot it cannot alter. */
    public void showRoundReport(List<PlayerStatusDto> statuses, Integer mysteryIndex, int roundsLeft) {
        for (PlayerStatusDto status : statuses) {
            print(status.colour() + " player now has " + status.piecesOnBoard()
                    + "/" + GameRules.PIECES_PER_PLAYER + " pieces on the board and "
                    + status.piecesInBase() + "/" + GameRules.PIECES_PER_PLAYER
                    + " pieces on the base.");
            print("============================");
            print("Location of pieces " + status.colour());
            print("============================");
            for (PieceLocationDto piece : status.pieces()) {
                print("Piece " + piece.pieceName() + " -> " + piece.location());
            }
        }
        if (mysteryIndex != null) {
            print("The mystery cell is at " + mysteryIndex
                    + " and will be there for the next " + roundsLeft + " rounds.");
        }
    }

    public void showWinner(Player winner) {
        print(winner.colour().displayName() + " player wins!!!");
    }

    public void showFinalPlacings(List<String> placings) {
        print("Final placings: " + String.join(", ", placings));
    }

    private String counts(Player player) {
        return player.colour().displayName() + " player now has " + player.piecesOnBoard()
                + "/" + GameRules.PIECES_PER_PLAYER + " pieces on the board and "
                + player.piecesInBase() + "/" + GameRules.PIECES_PER_PLAYER
                + " pieces on the base.";
    }

    private String describe(Direction direction) {
        return direction == Direction.CLOCKWISE ? "clockwise" : "counterclockwise";
    }

    private void print(String message) {
        System.out.println(message);
    }
}
