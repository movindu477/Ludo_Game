package lk.apiit.ludot;

import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.dto.GameResultDto;
import lk.apiit.ludot.engine.RandomDice;
import lk.apiit.ludot.gateway.GameResultGateway;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FacadeTest {

    /* An in-memory gateway, so the test writes no files. This is the
       payoff of GameResultGateway being an interface. */
    private static class InMemoryGateway implements GameResultGateway {
        private final List<GameResultDto> saved = new ArrayList<>();

        @Override
        public void insert(GameResultDto result) {
            saved.add(result);
        }

        @Override
        public List<GameResultDto> findAll() {
            return List.copyOf(saved);
        }
    }

    @Test
    void aFullGameRunsToCompletionAndIsSaved() {
        // Rule 11 - the game continues past the winner to find all places
        InMemoryGateway gateway = new InMemoryGateway();
        LudoGameFacade game = new LudoGameFacade(gateway, new RandomDice());

        GameResultDto result = game.runGame();

        assertThat(result.rounds()).isGreaterThan(0);
        assertThat(gateway.findAll()).containsExactly(result);
    }

    @Test
    void everyGameProducesAFullSetOfPlacings() {
        // this is the test that would have caught the deadlock. The round
        // check matters most: the engine ranks unfinished players when the
        // limit is hit, so placings alone would pass even for a stuck game.
        for (int run = 0; run < 20; run++) {
            InMemoryGateway gateway = new InMemoryGateway();
            GameResultDto result =
                    new LudoGameFacade(gateway, new RandomDice()).runGame();

            assertThat(result.rounds()).isLessThan(GameRules.MAX_ROUNDS);
            assertThat(result.placings()).hasSize(4);
            assertThat(result.winner()).isNotEqualTo("none");
        }
    }
}
