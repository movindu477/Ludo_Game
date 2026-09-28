package lk.apiit.ludot.domain;

/* Which way round the board a piece travels.
   Rule T-1 decides this with a coin toss when the piece leaves the base. */
public enum Direction {

    CLOCKWISE,
    COUNTER_CLOCKWISE;

    /* Rule T-14: a Gamma teleport flips a clockwise piece. */
    public Direction opposite() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
