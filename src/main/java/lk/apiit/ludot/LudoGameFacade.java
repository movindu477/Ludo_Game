package lk.apiit.ludot;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.dto.GameResultDto;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.Dice;
import lk.apiit.ludot.engine.GameEngine;
import lk.apiit.ludot.engine.MysteryCell;
import lk.apiit.ludot.gateway.GameResultGateway;
import lk.apiit.ludot.strategy.PlayerFactory;

import java.util.List;

/* FACADE.

   Notes 3b: "When a component uses many components within a different
   subsystem, it has too much intimate knowledge about the subsystem. A
   facade removes that - the client is simpler, has fewer dependencies,
   and is cleaner."

   Without this, main() would create and wire eight objects and know the
   order they must be built in. With it, main() calls runGame().

   Notes 3f describe the same class from the other side: the facade
   "simply instantiates a command and executes it" - here it assembles the
   subsystem, runs the engine, and hands the result to the gateway. */
public class LudoGameFacade {

    private final GameResultGateway gateway;
    private final Dice dice;

    public LudoGameFacade(GameResultGateway gateway, Dice dice) {
        this.gateway = gateway;
        this.dice = dice;
    }

    /* The whole simulation, in one call. */
    public GameResultDto runGame() {
        Board board = new Board();
        ConsoleView view = new ConsoleView();
        MysteryCell mysteryCell = new MysteryCell();
        EffectFactory effects = new EffectFactory();

        CommandFactory commands = new CommandFactory(board, mysteryCell, effects, view);
        List<Player> players = new PlayerFactory().createAll();

        GameResultDto result =
                new GameEngine(board, players, dice, commands, mysteryCell, view).play();

        gateway.insert(result);     // DATA GATEWAY - the only persistence call
        return result;
    }

    /* Past results, for anyone who wants them. The caller never learns
       whether they came from a CSV file or a database. */
    public List<GameResultDto> previousResults() {
        return gateway.findAll();
    }
}
