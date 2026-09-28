package lk.apiit.ludot;

import lk.apiit.ludot.effect.BriefingEffect;
import lk.apiit.ludot.effect.MysteryEffect;
import lk.apiit.ludot.effect.NoEffect;
import lk.apiit.ludot.effect.SpeedEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EffectTest {

    @Test
    void energisedDoublesAndSickHalves() {
        // Rule T-12, integer division on the halving
        assertThat(new SpeedEffect(true).adjustSteps(3)).isEqualTo(6);
        assertThat(new SpeedEffect(false).adjustSteps(3)).isEqualTo(1);
    }

    @Test
    void anEffectExpiresAfterFourRoundsAndBecomesTheNullObject() {
        MysteryEffect effect = new SpeedEffect(true);
        for (int round = 0; round < 4; round++) {
            effect = effect.afterRound();
        }
        assertThat(effect).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void aBriefingPieceCannotMove() {
        // Rule T-13
        assertThat(new BriefingEffect().allowsMovement()).isFalse();
    }

    @Test
    void threeThreesInARowSendTheBriefedPieceToBase() {
        MysteryEffect effect = new BriefingEffect();
        effect = effect.afterRoll(3);
        effect = effect.afterRoll(3);
        assertThat(effect.sendsPieceToBase()).isFalse();

        effect = effect.afterRoll(3);
        assertThat(effect.sendsPieceToBase()).isTrue();
    }

    @Test
    void anInterruptedRunOfThreesResetsTheCount() {
        MysteryEffect effect = new BriefingEffect();
        effect = effect.afterRoll(3);
        effect = effect.afterRoll(5);      // run broken
        effect = effect.afterRoll(3);
        effect = effect.afterRoll(3);
        assertThat(effect.sendsPieceToBase()).isFalse();
    }
}
