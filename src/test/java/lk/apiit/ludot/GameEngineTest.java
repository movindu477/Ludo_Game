package lk.apiit.ludot;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.dto.GameResultDto;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.Dice;
import lk.apiit.ludot.engine.GameEngine;
import lk.apiit.ludot.engine.MysteryCell;
import lk.apiit.ludot.engine.RandomDice;
import lk.apiit.ludot.gateway.CsvGameResultGateway;
import lk.apiit.ludot.gateway.GameResultGateway;
import lk.apiit.ludot.strategy.PlayerFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    /* Keeps results in a list, so most tests write no files. */
    private static class InMemoryGateway implements GameResultGateway {
        private final List<GameResultDto> rows = new ArrayList<>();

        @Override
        public void insert(GameResultDto result) {
            rows.add(result);
        }

        @Override
        public List<GameResultDto> findAll() {
            return List.copyOf(rows);
        }
    }

    private GameResultDto playOne() {
        return new LudoGameFacade(new InMemoryGateway(), new RandomDice()).runGame();
    }

    /* Wires the engine by hand, the way the facade does, so a test can
       hand it a scripted dice. */
    private GameResultDto playWith(Dice dice) {
        Board board = new Board();
        ConsoleView view = new ConsoleView();
        MysteryCell mysteryCell = new MysteryCell();
        CommandFactory commands =
                new CommandFactory(board, mysteryCell, new EffectFactory(), view);

        return new GameEngine(board, new PlayerFactory().createAll(),
                dice, commands, mysteryCell, view).play();
    }

    private static String[] allColourNames() {
        return Arrays.stream(Colour.values()).map(Colour::displayName).toArray(String[]::new);
    }

    @Test
    void aGameReturnsAResult() {
        assertThat(playOne()).isNotNull();
    }

    @Test
    void aGameNamesAWinner() {
        // "none" is the engine's placeholder for an empty finish order
        assertThat(playOne().winner()).isNotBlank().isNotEqualTo("none");
    }

    @Test
    void allFourColoursArePlaced() {
        // Rule 11 - play continues past the winner
        assertThat(playOne().placings()).containsExactlyInAnyOrder(allColourNames());
    }

    @Test
    void theWinnerIsFirstInThePlacings() {
        GameResultDto result = playOne();
        assertThat(result.placings().get(0)).isEqualTo(result.winner());
    }

    @Test
    void noColourIsPlacedTwice() {
        assertThat(playOne().placings()).doesNotHaveDuplicates();
    }

    @Test
    void aGameFinishesWithinTheRoundLimit() {
        // positive alone is not enough: a stuck game still reports rounds
        // and, thanks to the fallback ranking, a full set of placings
        assertThat(playOne().rounds()).isPositive().isLessThan(GameRules.MAX_ROUNDS);
    }

    @Test
    void thePlacingsListCannotBeModified() {
        // the DTO copies its list, so nobody can rewrite a finished game
        assertThat(playOne().placings()).isUnmodifiable();
    }

    @Test
    void theResultIsStoredInTheGateway() {
        InMemoryGateway gateway = new InMemoryGateway();

        GameResultDto result = new LudoGameFacade(gateway, new RandomDice()).runGame();

        assertThat(gateway.findAll()).containsExactly(result);
    }

    @Test
    void aStoredResultReadsBackThroughTheCsvGateway(@TempDir Path folder) {
        LudoGameFacade game = new LudoGameFacade(
                new CsvGameResultGateway(folder.resolve("results.csv")), new RandomDice());

        GameResultDto result = game.runGame();

        assertThat(game.previousResults()).containsExactly(result);
    }

    @ParameterizedTest
    @EnumSource(Colour.class)
    void everyColourAppearsInThePlacings(Colour colour) {
        assertThat(playOne().placings()).contains(colour.displayName());
    }

    @RepeatedTest(12)
    void repeatedGamesAllTerminateWithFourPlacings() {
        GameResultDto result = playOne();
        assertThat(result.placings()).hasSize(Colour.values().length);
        assertThat(result.rounds()).isPositive().isLessThan(GameRules.MAX_ROUNDS);
    }

    @Test
    void aGameThatHitsTheRoundLimitStillRanksEveryone() {
        // LoadedDice rolls 1 forever once empty, so no piece ever leaves
        // base (Rule 2) and the safety limit is the only way out
        GameResultDto result = playWith(new LoadedDice());

        assertThat(result.rounds()).isEqualTo(GameRules.MAX_ROUNDS);
        assertThat(result.placings()).containsExactlyInAnyOrder(allColourNames());
        assertThat(result.winner()).isEqualTo(result.placings().get(0));
    }
}
