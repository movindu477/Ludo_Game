package lk.apiit.ludot.engine;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.command.GameCommand;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.GameRules;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.dto.GameResultDto;
import lk.apiit.ludot.dto.PieceLocationDto;
import lk.apiit.ludot.dto.PlayerStatusDto;
import lk.apiit.ludot.exception.InvalidMoveException;
import lk.apiit.ludot.selector.MoveSelector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/* Runs the rounds. It knows the shape of a turn but none of the details -
   the strategy picks the move, the command carries it out, the view prints.

   The chains are built once per player and kept, because Blue's cycling
   selector has to remember its position between turns. */
public class GameEngine {

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private final CommandFactory factory;
    private final MysteryCell mysteryCell;
    private final ConsoleView view;

    private final Map<Colour, MoveSelector> chains = new EnumMap<>(Colour.class);
    private final List<Player> finishOrder = new ArrayList<>();
    private int roundsPlayed;

    public GameEngine(Board board, List<Player> players, Dice dice,
                      CommandFactory factory, MysteryCell mysteryCell, ConsoleView view) {
        this.board = board;
        this.players = List.copyOf(players);
        this.dice = dice;
        this.factory = factory;
        this.mysteryCell = mysteryCell;
        this.view = view;

        for (Player player : players) {
            chains.put(player.colour(), player.strategy().buildChain(board, factory));
        }
    }

    public GameResultDto play() {
        players.forEach(view::showPlayerPieces);
        List<Player> order = decideTurnOrder();

        // Rule 11: play on past the winner until only one player is left
        // unfinished - that player has taken last place by elimination.
        while (roundsPlayed < GameRules.MAX_ROUNDS && finishOrder.size() < players.size() - 1) {
            playRound(order);
        }

        // Ranks whoever is still unfinished by pieces home. Normally that is
        // just the last-placed player; if the round limit is ever reached it
        // is everyone left, so the simulation always produces a full result.
        if (finishOrder.size() < players.size()) {
            players.stream()
                    .filter(player -> !finishOrder.contains(player))
                    .sorted((a, b) -> Integer.compare(b.piecesHome(), a.piecesHome()))
                    .forEach(finishOrder::add);
        }

        view.showFinalPlacings(placingNames());
        return new GameResultDto(
                finishOrder.isEmpty() ? "none" : finishOrder.get(0).colour().displayName(),
                placingNames(), roundsPlayed);
    }

    /* Everyone rolls once; the highest starts. */
    private List<Player> decideTurnOrder() {
        Player best = players.get(0);
        int bestRoll = 0;
        for (Player player : players) {
            int roll = dice.roll();
            view.showOpeningRoll(player.colour(), roll);
            if (roll > bestRoll) {
                bestRoll = roll;
                best = player;
            }
        }
        List<Player> order = rotateTo(best);
        view.showFirstPlayer(best.colour(), order.stream().map(Player::colour).toList());
        return order;
    }

    private List<Player> rotateTo(Player first) {
        List<Player> order = new ArrayList<>();
        int start = players.indexOf(first);
        for (int i = 0; i < players.size(); i++) {
            order.add(players.get((start + i) % players.size()));
        }
        return order;
    }

    private void playRound(List<Player> order) {
        for (Player player : order) {
            if (!player.hasFinished()) {
                takeTurn(player);
                recordIfFinished(player);
            }
        }
        endRound();
    }

    /* One turn. The loop is Rule 4 and Rule T-2: a six or a capture earns
       another roll, and three sixes running ends the turn with nothing. */
    private void takeTurn(Player player) {
        int consecutiveSixes = 0;
        boolean rollAgain = true;

        while (rollAgain) {
            int roll = dice.roll();
            view.showRoll(player, roll);
            handleBriefings(player, roll);

            consecutiveSixes = roll == GameRules.ROLL_TO_LEAVE_BASE ? consecutiveSixes + 1 : 0;
            if (consecutiveSixes >= GameRules.MAX_CONSECUTIVE_SIXES) {
                breakAnyBlockade(player);          // Rules T-5 and T-6
                view.showForfeit(player);
                return;
            }

            GameCommand command = chains.get(player.colour()).select(player, roll);
            boolean captured = executeSafely(command, player);

            // a player whose last piece just reached Home has nothing left to roll for
            rollAgain = (roll == GameRules.ROLL_TO_LEAVE_BASE || captured)
                    && !player.hasFinished();
        }
    }

    /* Rule T-6: the forfeited turn also forces any blockade apart. */
    private void breakAnyBlockade(Player player) {
        List<Piece> blockade = board.blockadeOwnedBy(player.colour());
        if (!blockade.isEmpty()) {
            factory.breakBlockade(blockade).execute();
        }
    }

    /* Deck 2 slide 23: catch only what you can handle. An illegal move is
       recoverable - the turn simply passes - so it is caught here and
       nowhere else. */
    private boolean executeSafely(GameCommand command, Player player) {
        try {
            command.execute();
            if (command.isNoMove()) {
                view.showNoMove(player);
            }
            return command.grantsExtraRoll();
        } catch (InvalidMoveException rejected) {
            view.showNoMove(player);
            return false;
        }
    }

    /* Rule T-13: a briefing piece escapes to base on three threes. */
    private void handleBriefings(Player player, int roll) {
        for (Piece piece : player.pieces()) {
            piece.noteRoll(roll);
            if (piece.mustReturnToBase()) {
                board.lift(piece);
                piece.sendToBase();
                view.showBriefingEscape(piece);
            }
        }
    }

    private void recordIfFinished(Player player) {
        if (player.hasFinished() && !finishOrder.contains(player)) {
            finishOrder.add(player);
            if (finishOrder.size() == 1) {
                view.showWinner(player);       // Rule 11
            }
        }
    }

    private void endRound() {
        roundsPlayed++;
        players.forEach(Player::endRound);       // effects count down

        if (mysteryCell.onRoundEnd(board)) {
            view.showMysterySpawn(mysteryCell.location());
        }
        view.showRoundReport(statusSnapshot(), mysteryCell.location(), mysteryCell.roundsLeft());
    }

    /* Builds the DTOs. This is the boundary: past this point nothing can
       reach a live Piece. */
    private List<PlayerStatusDto> statusSnapshot() {
        List<PlayerStatusDto> snapshot = new ArrayList<>();
        for (Player player : players) {
            List<PieceLocationDto> locations = player.pieces().stream()
                    .map(piece -> new PieceLocationDto(piece.name(), piece.position().label()))
                    .toList();
            snapshot.add(new PlayerStatusDto(player.colour().displayName(),
                    player.piecesOnBoard(), player.piecesInBase(),
                    player.piecesHome(), locations));
        }
        return snapshot;
    }

    private List<String> placingNames() {
        return finishOrder.stream().map(p -> p.colour().displayName()).toList();
    }
}
