package lk.apiit.ludot;

import lk.apiit.ludot.engine.RandomDice;
import lk.apiit.ludot.gateway.CsvGameResultGateway;

import java.nio.file.Path;

/* The composition root - the only place that chooses concrete classes.

   Notice how little it knows. It picks a real dice and a CSV gateway,
   hands them to the facade, and calls one method. That is the facade
   doing its job. */
public class LudoTApplication {

    public static void main(String[] args) {
        LudoGameFacade game = new LudoGameFacade(
                new CsvGameResultGateway(Path.of("game-results.csv")),
                new RandomDice());

        game.runGame();
    }
}
