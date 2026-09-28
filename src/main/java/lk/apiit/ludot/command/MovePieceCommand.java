package lk.apiit.ludot.command;

import lk.apiit.ludot.domain.Board;
import lk.apiit.ludot.domain.Piece;
import lk.apiit.ludot.domain.Player;
import lk.apiit.ludot.domain.Position;
import lk.apiit.ludot.effect.EffectFactory;
import lk.apiit.ludot.engine.ConsoleView;
import lk.apiit.ludot.engine.MysteryCell;
import lk.apiit.ludot.exception.InvalidMoveException;

import java.util.List;

/* COMMAND - the ordinary move. Rules 1, 5, 6, 9, 10 and T-11.

   The method is kept short by handing each job to a helper, following the
   Clean Code rule that a function should do one thing at one level of
   abstraction (deck 1, slides 24-25). */
public class MovePieceCommand implements GameCommand {

    private final Piece piece;
    private final Player player;
    private final int steps;
    private final Board board;
    private final MysteryCell mysteryCell;
    private final EffectFactory effects;
    private final ConsoleView view;

    private boolean captured;

    public MovePieceCommand(Piece piece, Player player, int steps, Board board,
                            MysteryCell mysteryCell, EffectFactory effects,
                            ConsoleView view) {
        this.piece = piece;
        this.player = player;
        this.steps = steps;
        this.board = board;
        this.mysteryCell = mysteryCell;
        this.effects = effects;
        this.view = view;
    }

    @Override
    public void execute() {
        Position target = board.targetOf(piece, steps);
        if (target == null) {
            throw new InvalidMoveException(
                    piece.name() + " cannot move " + steps + " from " + piece.position());
        }
        Position from = piece.position();

        captureAnyOpponentsAt(target);
        if (board.passesApproach(piece, steps)) {
            piece.notePassedApproach();     // Rule T-1
        }
        relocate(target);

        view.showMove(piece, from, target, steps);
        if (target.isHome()) {
            view.showReachedHome(piece);   // Rule 10 - reached Home exactly
        }
        applyMysteryCellIfLanded(target);
    }

    /* Rule 6 and Rule T-9: anything of another colour on the target square
       goes back to its base, and this piece records the capture it needs
       for Rule T-7. */
    private void captureAnyOpponentsAt(Position target) {
        if (!target.isOnPath()) {
            return;
        }
        List<Piece> victims = board.opponentsAt(target.index(), piece.colour());
        for (Piece victim : victims) {
            board.lift(victim);
            victim.sendToBase();
            piece.recordCapture();
            captured = true;
            view.showCapture(player, piece, victim, target);
        }
    }

    private void relocate(Position target) {
        board.lift(piece);
        if (target.isOnPath()) {
            board.placeOnPath(piece, target.index());
        } else if (target.isOnHomeStraight()) {
            board.placeOnHomeStraight(piece, target.index());
        } else {
            board.sendHome(piece);
        }
    }

    /* Rule T-11: landing on the mystery cell teleports the piece and may
       leave a lasting effect. The factory decides which - this command
       does not need to know the six destinations. */
    private void applyMysteryCellIfLanded(Position target) {
        if (!target.isOnPath() || !mysteryCell.isAt(target.index())) {
            return;
        }
        mysteryCell.teleport(piece, board, effects, view);
    }

    /* Rule T-2: a capture earns another roll. */
    @Override
    public boolean grantsExtraRoll() {
        return captured;
    }
}
