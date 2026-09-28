package lk.apiit.ludot.engine;

/* Where dice values come from.

   This is an interface for one reason: a test can supply a fixed sequence
   and assert an exact outcome. Nothing in the domain or the commands ever
   rolls a Random dice, which is what makes every rule testable. */
public interface Dice {
    int roll();
}
