package lk.apiit.ludot.domain;

/* The four players.
   The declaration ORDER is load bearing: it is the clockwise order on the
   board, so ordinal() * 13 gives the starting square without a lookup table.
   Yellow 0, Blue 13, Red 26, Green 39. Re-ordering this breaks Board. */
public enum Colour {

    YELLOW("Y", "yellow"),
    BLUE("B", "blue"),
    RED("R", "red"),
    GREEN("G", "green");

    private final String letter;       // used in piece names like R1
    private final String displayName;  // used in the output messages

    Colour(String letter, String displayName) {
        this.letter = letter;
        this.displayName = displayName;
    }

    public String letter() {
        return letter;
    }

    public String displayName() {
        return displayName;
    }
}
