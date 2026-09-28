package lk.apiit.ludot;

import lk.apiit.ludot.engine.Dice;

import java.util.ArrayDeque;
import java.util.Queue;

/* A dice that returns exactly what a test asks for.

   This one small class is what makes every rule testable - without the
   Dice interface there would be no way to set up a specific situation. */
public class LoadedDice implements Dice {

    private final Queue<Integer> values = new ArrayDeque<>();

    public LoadedDice(int... rolls) {
        for (int roll : rolls) {
            values.add(roll);
        }
    }

    @Override
    public int roll() {
        return values.isEmpty() ? 1 : values.poll();   // 1 once the script runs out
    }
}
