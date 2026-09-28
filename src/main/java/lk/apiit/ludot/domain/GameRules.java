package lk.apiit.ludot.domain;

/* Every fixed number from the brief, in one place.
   Clean Code deck slide 7: a named constant hides a magic number,
   so no rule class ever contains a bare 52 or 13. */
public final class GameRules {

    public static final int STANDARD_CELLS = 52;       // white cells on the path
    public static final int HOME_STRAIGHT_CELLS = 5;   // coloured cells before Home
    public static final int PIECES_PER_PLAYER = 4;     // R1..R4
    public static final int CELLS_BETWEEN_STARTS = 13; // 52 / 4 players

    public static final int DICE_FACES = 6;
    public static final int ROLL_TO_LEAVE_BASE = 6;     // Rule 2
    public static final int MAX_CONSECUTIVE_SIXES = 3;  // Rule 4
    public static final int BRIEFING_ESCAPE_ROLL = 3;   // Rule T-13
    public static final int BRIEFING_ESCAPE_STREAK = 3; // Rule T-13, three in a row

    // Rule T-11: counted forward from the yellow approach cell
    public static final int ALPHA_OFFSET = 9;
    public static final int BETA_OFFSET = 27;
    public static final int GAMMA_OFFSET = 46;

    public static final int MYSTERY_LIFETIME_ROUNDS = 4; // Rule T-10
    public static final int EFFECT_DURATION_ROUNDS = 4;  // Rules T-12, T-13
    public static final int ROUNDS_BEFORE_MYSTERY = 2;   // Rule T-10

    // not from the brief: a safety limit so a stuck game cannot loop forever
    public static final int MAX_ROUNDS = 2000;

    private GameRules() {
        // constants only, nothing to instantiate
    }
}
