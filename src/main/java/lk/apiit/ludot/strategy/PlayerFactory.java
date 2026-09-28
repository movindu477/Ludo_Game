package lk.apiit.ludot.strategy;

import lk.apiit.ludot.domain.Colour;
import lk.apiit.ludot.domain.Player;

import java.util.ArrayList;
import java.util.List;

/* FACTORY METHOD.

   Notes 3e: "Can use a parameter to tell the method which kind of object
   to create". The colour is the parameter, and this is the only place that
   knows which strategy belongs to which colour.

   Adding a fifth behaviour means one new class and one new case here -
   nothing else in the project changes. */
public class PlayerFactory {

    public Player create(Colour colour) {
        return new Player(colour, strategyFor(colour));
    }

    public List<Player> createAll() {
        List<Player> players = new ArrayList<>();
        for (Colour colour : Colour.values()) {
            players.add(create(colour));
        }
        return players;
    }

    private PlayerStrategy strategyFor(Colour colour) {
        return switch (colour) {
            case RED -> new RedStrategy();
            case GREEN -> new GreenStrategy();
            case YELLOW -> new YellowStrategy();
            case BLUE -> new BlueStrategy();
        };
    }
}
