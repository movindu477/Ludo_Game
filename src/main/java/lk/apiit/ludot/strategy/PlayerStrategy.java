package lk.apiit.ludot.strategy;

import lk.apiit.ludot.command.CommandFactory;
import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.selector.MoveSelector;

/* STRATEGY - the interface.

   Section 2.1 of the brief gives four different decision policies over
   identical information. Without this, the turn loop would carry a switch
   on colour, and adding a fifth player would mean editing it.

   Each strategy expresses itself by building its own chain of selectors,
   so the two patterns work together: Strategy chooses the priorities,
   Chain of Responsibility applies them in order. */
public interface PlayerStrategy {

    MoveSelector buildChain(Board board, CommandFactory factory);

    String name();
}
