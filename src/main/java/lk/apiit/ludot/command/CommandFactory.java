package lk.apiit.ludot.command;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Direction;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.MysteryCell;

import java.util.List;
import java.util.Random;

/* FACTORY METHOD.

   Notes 3e: the motivation is that "a class can be instantiated many times
   in many places - many opportunities for coding errors, many places to
   make changes if the constructor has to change". Every command in this
   project is built here and nowhere else.

   Notes 3f also says the Command pattern "can make use of the Factory
   Method pattern", which is exactly this pairing: the selector chain asks
   the factory for a command and executes it, without ever using "new". */
public class CommandFactory {

    private final Board board;
    private final MysteryCell mysteryCell;
    private final EffectFactory effects;
    private final ConsoleView view;
    private final Random coin = new Random();   // Rule T-1 coin toss only

    public CommandFactory(Board board, MysteryCell mysteryCell,
                          EffectFactory effects, ConsoleView view) {
        this.board = board;
        this.mysteryCell = mysteryCell;
        this.effects = effects;
        this.view = view;
    }

    public GameCommand enterBoard(Piece piece, Player player) {
        Direction tossed = coin.nextBoolean()
                ? Direction.CLOCKWISE
                : Direction.COUNTER_CLOCKWISE;
        return new EnterBoardCommand(piece, player, board, tossed, view);
    }

    public GameCommand movePiece(Piece piece, Player player, int steps) {
        return new MovePieceCommand(piece, player, steps, board,
                mysteryCell, effects, view);
    }

    public GameCommand moveBlock(List<Piece> members, int roll) {
        return new MoveBlockCommand(members, roll, board, view);
    }

    /* Rule T-6 - built here like every other command. */
    public GameCommand breakBlockade(List<Piece> members) {
        return new BreakBlockadeCommand(members, board, view);
    }

    /* NULL OBJECT: the factory never returns null, so no caller tests. */
    public GameCommand noMove() {
        return NullMoveCommand.INSTANCE;
    }
}
