package lk.apiit.ludot;

import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.effect.BriefingEffect;
import lk.apiit.ludot.effect.MysteryEffect;
import lk.apiit.ludot.effect.NoEffect;
import lk.apiit.ludot.effect.SpeedEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/* Rules T-12 and T-13 - what each effect does, and when it wears off. */
class EffectLifecycleTest {

    private static MysteryEffect ageBy(MysteryEffect effect, int rounds) {
        for (int i = 0; i < rounds; i++) {
            effect = effect.afterRound();
        }
        return effect;
    }

    @ParameterizedTest
    @CsvSource({"1, 2", "2, 4", "3, 6", "4, 8", "5, 10", "6, 12"})
    void anEnergisedPieceMovesTwiceAsFar(int roll, int expected) {
        assertThat(new SpeedEffect(true).adjustSteps(roll)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "2, 1", "3, 1", "4, 2", "5, 2", "6, 3"})
    void aSickPieceMovesHalfAsFarRoundedDown(int roll, int expected) {
        assertThat(new SpeedEffect(false).adjustSteps(roll)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void aBriefedPieceGoesNowhereWhateverTheRoll(int roll) {
        assertThat(new BriefingEffect().adjustSteps(roll)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void anEnergisedEffectSurvivesTheFirstThreeRounds(int rounds) {
        assertThat(ageBy(new SpeedEffect(true), rounds)).isInstanceOf(SpeedEffect.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void aSickEffectSurvivesTheFirstThreeRounds(int rounds) {
        assertThat(ageBy(new SpeedEffect(false), rounds)).isInstanceOf(SpeedEffect.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void aBriefingSurvivesTheFirstThreeRounds(int rounds) {
        assertThat(ageBy(new BriefingEffect(), rounds)).isInstanceOf(BriefingEffect.class);
    }

    @Test
    void anEnergisedEffectExpiresAfterFourRounds() {
        assertThat(ageBy(new SpeedEffect(true), 4)).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void aSickEffectExpiresAfterFourRounds() {
        assertThat(ageBy(new SpeedEffect(false), 4)).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void aBriefingExpiresAfterFourRounds() {
        assertThat(ageBy(new BriefingEffect(), 4)).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void noEffectNeverExpires() {
        assertThat(ageBy(NoEffect.INSTANCE, 10)).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void noEffectAllowsMovement() {
        assertThat(NoEffect.INSTANCE.allowsMovement()).isTrue();
    }

    @Test
    void anEnergisedPieceCanStillMove() {
        assertThat(new SpeedEffect(true).allowsMovement()).isTrue();
    }

    @Test
    void aSickPieceCanStillMove() {
        assertThat(new SpeedEffect(false).allowsMovement()).isTrue();
    }

    @Test
    void aBriefedPieceCannotMove() {
        assertThat(new BriefingEffect().allowsMovement()).isFalse();
    }

    @Test
    void aBriefedPieceMayMoveAgainOnceTheBriefingEnds() {
        Piece piece = new Piece(Colour.GREEN, 1);
        piece.applyEffect(new BriefingEffect());
        assertThat(piece.canMove()).isFalse();

        for (int round = 0; round < 4; round++) {
            piece.endRound();
        }

        assertThat(piece.canMove()).isTrue();
        assertThat(piece.effect()).isSameAs(NoEffect.INSTANCE);
    }

    @Test
    void noEffectDoesNotSendAPieceToBase() {
        assertThat(NoEffect.INSTANCE.sendsPieceToBase()).isFalse();
    }

    @Test
    void threeThreesOnlyMatterDuringABriefing() {
        // Rule T-13's escape belongs to the briefing alone
        MysteryEffect speed = new SpeedEffect(true);
        for (int i = 0; i < GameRules.BRIEFING_ESCAPE_STREAK; i++) {
            speed = speed.afterRoll(GameRules.BRIEFING_ESCAPE_ROLL);
        }
        assertThat(speed.sendsPieceToBase()).isFalse();
    }

    @Test
    void noEffectDescribesItselfAsNormal() {
        assertThat(NoEffect.INSTANCE.describe()).isEqualTo("normal");
    }

    @Test
    void everyEffectDescribesItself() {
        // the words that appear in the round report
        assertThat(new SpeedEffect(true).describe()).isEqualTo("energized");
        assertThat(new SpeedEffect(false).describe()).isEqualTo("sick");
        assertThat(new BriefingEffect().describe()).isEqualTo("in briefing");
    }
}
