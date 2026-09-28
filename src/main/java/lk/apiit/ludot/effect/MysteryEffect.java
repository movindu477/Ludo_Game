package lk.apiit.ludot.effect;

/* What a mystery cell leaves behind on a piece (Rules T-12 and T-13).

   A piece always holds one of these and it is never null, so no movement
   code ever asks "is this piece sick or energised" - it just asks the
   effect and gets an answer. */
public interface MysteryEffect {

    /* Rule T-12: energised doubles the roll, sick halves it. */
    int adjustSteps(int roll);

    /* Rule T-13: false while the piece is attending a briefing. */
    boolean allowsMovement();

    /* Rule T-13: the briefing counts threes, so every roll is reported.
       Returns whatever the effect becomes next. */
    MysteryEffect afterRoll(int roll);

    /* Rules T-12 and T-13: effects last four rounds, then expire. */
    MysteryEffect afterRound();

    /* Rule T-13: three threes in a row sends the piece back to base. */
    boolean sendsPieceToBase();

    String describe();
}
