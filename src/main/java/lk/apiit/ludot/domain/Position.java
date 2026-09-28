package lk.apiit.ludot.domain;

import java.util.Objects;

/* Where a piece is.
   A plain int cannot do this job: cell 3 of the standard path and cell 3 of
   a home straight are different squares, and base and home are not cells at
   all. Zone plus index says exactly which.

   Immutable, and built only through the four factory methods, so an invalid
   position cannot be created. */
public final class Position {

    /* Nested so the two ideas stay together - a Position is always a zone
       plus an index, and Zone has no meaning on its own. */
    public enum Zone { BASE, STANDARD_PATH, HOME_STRAIGHT, HOME }

    private final Zone zone;
    private final int index;
    private final Colour owner;   // only meaningful off the standard path

    private Position(Zone zone, int index, Colour owner) {
        this.zone = zone;
        this.index = index;
        this.owner = owner;
    }

    public static Position inBase(Colour owner) {
        return new Position(Zone.BASE, -1, owner);
    }

    public static Position onPath(int index) {
        if (index < 0 || index >= GameRules.STANDARD_CELLS) {
            throw new IllegalArgumentException("Path index out of range: " + index);
        }
        return new Position(Zone.STANDARD_PATH, index, null);
    }

    public static Position homeStraight(Colour owner, int step) {
        if (step < 0 || step >= GameRules.HOME_STRAIGHT_CELLS) {
            throw new IllegalArgumentException("Home straight step out of range: " + step);
        }
        return new Position(Zone.HOME_STRAIGHT, step, owner);
    }

    public static Position home(Colour owner) {
        return new Position(Zone.HOME, -1, owner);
    }

    public int index() {
        return index;
    }

    public boolean isInBase() {
        return zone == Zone.BASE;
    }

    public boolean isOnPath() {
        return zone == Zone.STANDARD_PATH;
    }

    public boolean isOnHomeStraight() {
        return zone == Zone.HOME_STRAIGHT;
    }

    public boolean isHome() {
        return zone == Zone.HOME;
    }

    /* The square name the brief's output messages use:
       plain numbers on the path, "greenhomepath2" on a home straight. */
    public String label() {
        return switch (zone) {
            case BASE -> "Base";
            case HOME -> "Home";
            case HOME_STRAIGHT -> owner.displayName() + "homepath" + index;
            case STANDARD_PATH -> String.valueOf(index);
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Position that)) {
            return false;
        }
        return zone == that.zone && index == that.index && owner == that.owner;
    }

    @Override
    public int hashCode() {
        return Objects.hash(zone, index, owner);
    }

    @Override
    public String toString() {
        return label();
    }
}
