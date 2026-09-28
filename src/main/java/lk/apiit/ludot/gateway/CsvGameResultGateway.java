package lk.apiit.ludot.gateway;

import lk.apiit.ludot.dto.GameResultDto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* DATA GATEWAY - the CSV implementation.

   Notes 3d: "Holds all the SQL for accessing a single table... Essentially
   a wrapper class for SQL statements". We have no database, so this wraps
   file access instead - but the point stands: this is the ONLY class in
   the project that touches the file system.

   Error handling follows Clean Code deck 2:
   - unchecked exception, so callers are not forced into try/catch (slide 16)
   - the original IOException is kept as the cause, so the stack trace
     survives (slide 21)
   - nothing is swallowed, and nothing calls printStackTrace (slides 18, 24) */
public class CsvGameResultGateway implements GameResultGateway {

    private static final String HEADER = "winner,placings,rounds";
    private static final String FIELD_SEPARATOR = ",";
    private static final String PLACING_SEPARATOR = " ";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public CsvGameResultGateway(Path file) {
        this.file = file;
    }

    @Override
    public void insert(GameResultDto result) {
        try {
            if (Files.notExists(file)) {
                Files.writeString(file, HEADER + System.lineSeparator(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            }
            String row = result.winner() + FIELD_SEPARATOR
                    + String.join(PLACING_SEPARATOR, result.placings()) + FIELD_SEPARATOR
                    + result.rounds() + System.lineSeparator();

            Files.writeString(file, row, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);

        } catch (IOException cause) {
            // context added, cause preserved - deck 2 slides 21 and 26
            throw new UncheckedIOException(
                    "Could not write the game result to " + file, cause);
        }
    }

    @Override
    public List<GameResultDto> findAll() {
        List<GameResultDto> results = new ArrayList<>();
        if (Files.notExists(file)) {
            return results;                    // empty list, never null
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank() || line.equals(HEADER)) {
                    continue;
                }
                String[] parts = line.split(FIELD_SEPARATOR, -1);
                if (parts.length == FIELD_COUNT) {
                    results.add(new GameResultDto(parts[0],
                            parsePlacings(parts[1]),
                            Integer.parseInt(parts[2])));
                }
            }
            return results;

        } catch (IOException cause) {
            throw new UncheckedIOException("Could not read " + file, cause);
        }
    }

    /* An unfinished game has no placings; that must read back as an empty
       list, not a list holding one empty string. */
    private List<String> parsePlacings(String field) {
        return field.isBlank()
                ? List.of()
                : Arrays.asList(field.split(PLACING_SEPARATOR));
    }
}
