package lk.apiit.ludot.effect;

import lk.apiit.ludot.domain.GameRules;

/* Rule T-12 - the Alpha aura.

   One class covers both outcomes, because energised and sick differ only
   in what they do to the number. Two near-identical classes would be the
   duplication the clean code notes warn about.

   The halving uses integer division on purpose, so a sick piece rolling a
   three moves one cell. */
public class SpeedEffect implements MysteryEffect {

    private final boolean energised;
    private int roundsRemaining = GameRules.EFFECT_DURATION_ROUNDS;

    public SpeedEffect(boolean energised) {
        this.energised = energised;
    }

    @Override
    public int adjustSteps(int roll) {
        return energised ? roll * 2 : roll / 2;
    }

    @Override
    public boolean allowsMovement() {
        return true;
    }

    @Override
    public MysteryEffect afterRoll(int roll) {
        return this;
    }

    @Override
    public MysteryEffect afterRound() {
        roundsRemaining--;
        // once the four rounds are spent, the piece goes back to normal -
        // and the Null Object is what "normal" is
        return roundsRemaining <= 0 ? NoEffect.INSTANCE : this;
    }

    @Override
    public boolean sendsPieceToBase() {
        return false;
    }

    @Override
    public String describe() {
        return energised ? "energized" : "sick";
    }
}
