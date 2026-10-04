package lk.apiit.ludot;

import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.effect.NoEffect;
import lk.apiit.ludot.strategy.PlayerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/* The state every piece is in before the first roll. */
class PieceStateTest {

    private Player playerOf(Colour colour) {
        return new PlayerFactory().create(colour);
    }

    private List<Piece> piecesOf(Colour colour) {
        return playerOf(colour).pieces();
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void everyPlayerOwnsFourPieces(Colour colour) {
        assertThat(playerOf(colour).pieces()).hasSize(GameRules.PIECES_PER_PLAYER);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void everyPieceStartsInItsBase(Colour colour) {
        assertThat(piecesOf(colour)).allMatch(p -> p.position().isInBase());
        assertThat(playerOf(colour).piecesInBase()).isEqualTo(GameRules.PIECES_PER_PLAYER);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void everyPieceStartsWithNoEffect(Colour colour) {
        assertThat(piecesOf(colour)).allMatch(p -> p.effect() == NoEffect.INSTANCE);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void aPieceWithNoEffectMayMove(Colour colour) {
        assertThat(piecesOf(colour)).allMatch(Piece::canMove);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void aPieceKnowsItsOwnColour(Colour colour) {
        assertThat(piecesOf(colour)).allMatch(p -> p.colour() == colour);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void piecesAreNamedByColourLetterAndNumber(Colour colour) {
        // the names the brief's output messages use, e.g. R1..R4
        String l = colour.letter();
        assertThat(piecesOf(colour)).extracting(Piece::name)
                .containsExactly(l + 1, l + 2, l + 3, l + 4);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void noEffectLeavesTheRollUnchanged(int roll) {
        assertThat(piecesOf(Colour.RED).get(0).stepsFor(roll)).isEqualTo(roll);
    }

    @Test
    void thereAreSixteenPiecesInTotal() {
        int total = new PlayerFactory().createAll().stream()
                .mapToInt(p -> p.pieces().size())
                .sum();
        assertThat(total).isEqualTo(16);
    }

    @Test
    void theFactoryCreatesOnePlayerPerColourInClockwiseOrder() {
        // Board relies on this order for the start squares
        assertThat(new PlayerFactory().createAll()).extracting(Player::colour)
                .containsExactly(Colour.YELLOW, Colour.BLUE, Colour.RED, Colour.GREEN);
    }
}
