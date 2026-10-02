package lk.apiit.ludot;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.effect.SpeedEffect;
import lk.apiit.ludot.engine.RandomDice;
import lk.apiit.ludot.strategy.PlayerFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTableTest {

    @ParameterizedTest
    @EnumSource(Colour.class)
    void startSquareIsThirteenApartForEveryColour(Colour colour) {
        assertThat(new Board().startIndexOf(colour))
                .isEqualTo(colour.ordinal() * GameRules.CELLS_BETWEEN_STARTS);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void approachCellSitsOneSquareBeforeStart(Colour colour) {
        Board board = new Board();
        int start = board.startIndexOf(colour);
        int expected = Math.floorMod(start - 1, GameRules.STANDARD_CELLS);
        assertThat(board.approachIndexOf(colour)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, 39, 51})
    void clockwiseStepWrapsAtTheEndOfThePath(int from) {
        Board board = new Board();
        int to = board.step(from, Direction.CLOCKWISE, 1);
        assertThat(to).isEqualTo((from + 1) % GameRules.STANDARD_CELLS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, 39, 51})
    void counterClockwiseStepWrapsBackwards(int from) {
        Board board = new Board();
        int to = board.step(from, Direction.COUNTER_CLOCKWISE, 1);
        assertThat(to).isEqualTo(Math.floorMod(from - 1, GameRules.STANDARD_CELLS));
    }

    @RepeatedTest(5)
    void diceNeverRollsOutsideOneToSix() {
        int roll = new RandomDice().roll();
        assertThat(roll).isBetween(1, GameRules.DICE_FACES);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void speedEffectDoublesEveryDiceValue(int roll) {
        SpeedEffect effect = new SpeedEffect(true);
        assertThat(effect.adjustSteps(roll)).isEqualTo(roll * 2);
    }

    @ParameterizedTest
    @CsvSource({
            "YELLOW, home-seeking",
            "BLUE, random",
            "RED, aggressive",
            "GREEN, blocking"
    })
    void factoryGivesEachColourItsOwnStrategy(Colour colour, String strategyName) {
        Player player = new PlayerFactory().create(colour);
        assertThat(player.colour()).isEqualTo(colour);
        assertThat(player.strategy()).isNotNull();
        assertThat(player.strategy().name()).isEqualTo(strategyName);
    }
}
