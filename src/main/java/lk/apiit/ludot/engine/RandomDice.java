package lk.apiit.ludot.engine;

import lk.apiit.ludot.domain.GameRules;

import java.util.Random;

/* The real dice, used when the simulation runs for real. */
public class RandomDice implements Dice {

    private final Random random = new Random();

    @Override
    public int roll() {
        return random.nextInt(GameRules.DICE_FACES) + 1;
    }
}
