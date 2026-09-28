package lk.apiit.ludot.effect;

import lk.apiit.ludot.domain.GameRules;

/* Rule T-13 - the Beta briefing.

   The piece cannot move for four rounds. There is one escape: if the
   player rolls a three, three times running, the piece is sent back to
   base instead of waiting it out. */
public class BriefingEffect implements MysteryEffect {

    private int roundsRemaining = GameRules.EFFECT_DURATION_ROUNDS;
    private int consecutiveThrees;

    @Override
    public int adjustSteps(int roll) {
        return 0;                       // it is not going anywhere
    }

    @Override
    public boolean allowsMovement() {
        return false;
    }

    @Override
    public MysteryEffect afterRoll(int roll) {
        if (roll == GameRules.BRIEFING_ESCAPE_ROLL) {
            consecutiveThrees++;
        } else {
            consecutiveThrees = 0;      // the run is broken
        }
        return this;
    }

    @Override
    public MysteryEffect afterRound() {
        roundsRemaining--;
        return roundsRemaining <= 0 ? NoEffect.INSTANCE : this;
    }

    @Override
    public boolean sendsPieceToBase() {
        return consecutiveThrees >= GameRules.BRIEFING_ESCAPE_STREAK;
    }

    @Override
    public String describe() {
        return "in briefing";
    }
}
