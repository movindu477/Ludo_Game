package lk.apiit.ludot;

import lk.apiit.ludot.dto.GameResultDto;
import lk.apiit.ludot.gateway.CsvGameResultGateway;
import lk.apiit.ludot.gateway.GameResultGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayTest {

    @Test
    void aResultCanBeWrittenAndReadBack(@TempDir Path folder) {
        GameResultGateway gateway =
                new CsvGameResultGateway(folder.resolve("results.csv"));

        gateway.insert(new GameResultDto("red", List.of("red", "green", "yellow", "blue"), 42));
        List<GameResultDto> saved = gateway.findAll();

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).winner()).isEqualTo("red");
        assertThat(saved.get(0).placings()).containsExactly("red", "green", "yellow", "blue");
        assertThat(saved.get(0).rounds()).isEqualTo(42);
    }

    @Test
    void readingBeforeAnythingIsSavedGivesAnEmptyListNotNull(@TempDir Path folder) {
        // Clean Code deck 2, slide 28 - never return null
        GameResultGateway gateway =
                new CsvGameResultGateway(folder.resolve("missing.csv"));

        assertThat(gateway.findAll()).isEmpty();
    }
}
