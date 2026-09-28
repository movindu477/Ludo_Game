package lk.apiit.ludot.domain;

import lk.apiit.ludot.strategy.PlayerStrategy;

import java.util.ArrayList;
import java.util.List;

/* One player: a colour, four pieces, and the strategy that decides its moves.

   The strategy is held as an interface, so Player never knows whether it is
   playing as Red or Blue - that is the whole point of the Strategy pattern. */
public class Player {

    private final Colour colour;
    private final List<Piece> pieces = new ArrayList<>();
    private final PlayerStrategy strategy;

    public Player(Colour colour, PlayerStrategy strategy) {
        this.colour = colour;
        this.strategy = strategy;
        for (int n = 1; n <= GameRules.PIECES_PER_PLAYER; n++) {
            pieces.add(new Piece(colour, n));
        }
    }

    public Colour colour() {
        return colour;
    }

    public List<Piece> pieces() {
        return List.copyOf(pieces);
    }

    public PlayerStrategy strategy() {
        return strategy;
    }

    public int piecesInBase() {
        return (int) pieces.stream().filter(Piece::isInBase).count();
    }

    public int piecesHome() {
        return (int) pieces.stream().filter(Piece::isHome).count();
    }

    public int piecesOnBoard() {
        return GameRules.PIECES_PER_PLAYER - piecesInBase() - piecesHome();
    }

    /* Rule 11: all four pieces home means this player has finished. */
    public boolean hasFinished() {
        return piecesHome() == GameRules.PIECES_PER_PLAYER;
    }

    /* "R1, R2, R3, and R4" for the opening announcement. */
    public String pieceNames() {
        List<String> names = pieces.stream().map(Piece::name).toList();
        return String.join(", ", names.subList(0, names.size() - 1))
                + ", and " + names.get(names.size() - 1);
    }

    public void endRound() {
        pieces.forEach(Piece::endRound);
    }
}
