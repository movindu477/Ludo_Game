package lk.apiit.ludot.domain;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/* The board: 52 standard cells plus a five cell home straight per colour.

   A cell holds a LIST of pieces, not one, because Rule T-3 lets two or more
   same coloured pieces share a square and form a block.

   All movement arithmetic is in step(). Keeping it in one method is why
   Rule T-1 needed no special cases anywhere else in the project. */
public class Board {

    private static final int BLOCK_SIZE = 2;   // Rule T-3: two or more pieces

    private final List<List<Piece>> path = new ArrayList<>();
    private final Map<Colour, List<List<Piece>>> homeStraights = new EnumMap<>(Colour.class);
    private final Map<Colour, Integer> startIndexes = new EnumMap<>(Colour.class);
    private final Map<Colour, Integer> approachIndexes = new EnumMap<>(Colour.class);

    public Board() {
        for (int i = 0; i < GameRules.STANDARD_CELLS; i++) {
            path.add(new ArrayList<>());
        }
        for (Colour colour : Colour.values()) {
            List<List<Piece>> straight = new ArrayList<>();
            for (int i = 0; i < GameRules.HOME_STRAIGHT_CELLS; i++) {
                straight.add(new ArrayList<>());
            }
            homeStraights.put(colour, straight);

            // Colour is declared in clockwise order, so its position times 13
            // gives the starting square: Yellow 0, Blue 13, Red 26, Green 39.
            int start = colour.ordinal() * GameRules.CELLS_BETWEEN_STARTS;
            startIndexes.put(colour, start);

            // The approach cell is the last path cell before the home straight,
            // which is the square just behind the start. floorMod wraps yellow
            // back to 51 instead of giving -1.
            approachIndexes.put(colour,
                    Math.floorMod(start - 1, GameRules.STANDARD_CELLS));
        }
    }

    public int startIndexOf(Colour colour) {
        return startIndexes.get(colour);
    }

    public int approachIndexOf(Colour colour) {
        return approachIndexes.get(colour);
    }

    /* The only movement arithmetic in the project. Adds when clockwise,
       subtracts when not, and floorMod wraps it round 52 in both cases. */
    public int step(int from, Direction direction, int steps) {
        int delta = direction == Direction.CLOCKWISE ? steps : -steps;
        return Math.floorMod(from + delta, GameRules.STANDARD_CELLS);
    }

    public List<Piece> piecesAt(int index) {
        return List.copyOf(path.get(index));
    }

    public boolean isEmpty(int index) {
        return path.get(index).isEmpty();
    }

    /* Rule T-3: two or more pieces of one colour on a cell is a block.
       Counted per colour, because entering the board or a teleport can
       leave pieces of different colours sharing a square. */
    public boolean hasBlockAt(int index) {
        for (Colour colour : Colour.values()) {
            if (countOf(colour, index) >= BLOCK_SIZE) {
                return true;
            }
        }
        return false;
    }

    /* Rule T-3: only an opponent's block stops a piece, never your own. */
    public boolean isBlockedFor(int index, Colour mover) {
        for (Colour colour : Colour.values()) {
            if (colour != mover && countOf(colour, index) >= BLOCK_SIZE) {
                return true;
            }
        }
        return false;
    }

    /* Rule 6: pieces here that the mover could capture. A block cannot be
       captured by a single piece (Rule T-8), so a blocked cell gives none. */
    public List<Piece> opponentsAt(int index, Colour mover) {
        if (isBlockedFor(index, mover)) {
            return List.of();
        }
        List<Piece> opponents = new ArrayList<>();
        for (Piece piece : path.get(index)) {
            if (piece.colour() != mover) {
                opponents.add(piece);
            }
        }
        return List.copyOf(opponents);
    }

    private int countOf(Colour colour, int index) {
        int count = 0;
        for (Piece piece : path.get(index)) {
            if (piece.colour() == colour) {
                count++;
            }
        }
        return count;
    }

    public void placeOnPath(Piece piece, int index) {
        path.get(index).add(piece);
        piece.moveTo(Position.onPath(index));
    }

    public void placeOnHomeStraight(Piece piece, int step) {
        homeStraights.get(piece.colour()).get(step).add(piece);
        piece.moveTo(Position.homeStraight(piece.colour(), step));
    }

    public void sendHome(Piece piece) {
        piece.moveTo(Position.home(piece.colour()));
    }

    /* Takes a piece off whatever square it is on. Safe to call for a piece
       in the base or already home - there is nothing to remove. */
    public void lift(Piece piece) {
        Position current = piece.position();
        if (current.isOnPath()) {
            path.get(current.index()).remove(piece);
        } else if (current.isOnHomeStraight()) {
            homeStraights.get(piece.colour()).get(current.index()).remove(piece);
        }
    }

    /* Rule T-10: the mystery cell may only appear on an empty path cell. */
    public List<Integer> emptyPathCells() {
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < GameRules.STANDARD_CELLS; i++) {
            if (path.get(i).isEmpty()) {
                free.add(i);
            }
        }
        return free;
    }

    /* Rule T-10: the cell waits until pieces are actually on the path. */
    public int pieceCountOnPath() {
        int total = 0;
        for (List<Piece> cell : path) {
            total += cell.size();
        }
        return total;
    }

    /* Where a move would land, or null when the roll overshoots Home.
       Returning null here is deliberate: this is the one place the code
       asks "is this move possible at all", and the caller turns a null
       into a NullMoveCommand rather than propagating it. */
    public Position targetOf(Piece piece, int steps) {
        if (steps <= 0) {
            return null;
        }
        Position current = piece.position();

        if (current.isOnHomeStraight()) {
            int target = current.index() + steps;
            if (target < GameRules.HOME_STRAIGHT_CELLS) {
                return Position.homeStraight(piece.colour(), target);
            }
            if (target == GameRules.HOME_STRAIGHT_CELLS) {
                return Position.home(piece.colour());
            }
            return null;                      // Rule 10 - must be exact
        }
        if (!current.isOnPath()) {
            return null;
        }

        int toApproach = stepsToApproach(piece);
        if (steps <= toApproach) {
            return Position.onPath(step(current.index(), piece.direction(), steps));
        }
        // Rule T-7 needs a capture, Rule T-1 needs the second approach pass.
        // Without both the piece may not turn in, so it simply carries on
        // round the board instead.
        if (!piece.hasCaptured() || !piece.mayEnterHomeStraight()) {
            return Position.onPath(step(current.index(), piece.direction(), steps));
        }
        int beyond = steps - toApproach;
        if (beyond <= GameRules.HOME_STRAIGHT_CELLS) {
            return Position.homeStraight(piece.colour(), beyond - 1);
        }
        if (beyond == GameRules.HOME_STRAIGHT_CELLS + 1) {
            return Position.home(piece.colour());
        }
        return null;
    }

    /* How many path cells lie between the piece and its approach cell,
       measured in the direction it is travelling. */
    public int stepsToApproach(Piece piece) {
        int approach = approachIndexOf(piece.colour());
        int from = piece.position().index();
        if (piece.direction() == Direction.CLOCKWISE) {
            return Math.floorMod(approach - from, GameRules.STANDARD_CELLS);
        }
        return Math.floorMod(from - approach, GameRules.STANDARD_CELLS);
    }

    /* Rule T-1: does this move take the piece past its own approach cell? */
    public boolean passesApproach(Piece piece, int steps) {
        return piece.position().isOnPath() && steps > stepsToApproach(piece);
    }

    /* Rule T-3: an opponent block anywhere along the route stops the move,
       not just one sitting on the destination square. Only the path cells
       the piece really crosses are checked - once it turns into its home
       straight it has left the shared path. */
    public boolean pathIsBlocked(Piece piece, int steps) {
        if (!piece.position().isOnPath()) {
            return false;
        }
        boolean turnsIn = piece.hasCaptured() && piece.mayEnterHomeStraight();
        int cellsOnPath = turnsIn
                ? Math.min(steps, stepsToApproach(piece))
                : steps;
        return routeIsBlocked(piece.position().index(), piece.direction(),
                cellsOnPath, piece.colour());
    }

    /* Rule T-3 for any route along the path - shared by single pieces and
       by blocks moving as one unit (Rule T-4). */
    public boolean routeIsBlocked(int from, Direction direction, int steps, Colour mover) {
        for (int s = 1; s <= steps; s++) {
            if (isBlockedFor(step(from, direction, s), mover)) {
                return true;
            }
        }
        return false;
    }

    /* Rule T-6: this player's own blockade, or an empty list if they
       don't have one. Derived from cell occupancy, like every other
       block question, so there is no second source of truth. */
    public List<Piece> blockadeOwnedBy(Colour colour) {
        for (int index = 0; index < GameRules.STANDARD_CELLS; index++) {
            List<Piece> ours = piecesAt(index).stream()
                    .filter(piece -> piece.colour() == colour)
                    .toList();
            if (ours.size() >= BLOCK_SIZE) {
                return ours;
            }
        }
        return List.of();
    }

    /* Rule T-4: a block travels the way its member with the longest journey
       home is facing. A tie keeps the first member, so a seeded run stays
       repeatable - a random tie-break would make the tests unreliable. */
    public Piece furthestFromHome(List<Piece> members) {
        Piece furthest = members.get(0);
        for (Piece piece : members) {
            if (stepsToApproach(piece) > stepsToApproach(furthest)) {
                furthest = piece;
            }
        }
        return furthest;
    }
}
