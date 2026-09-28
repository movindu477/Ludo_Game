package lk.apiit.ludot.domain;

import lk.apiit.ludot.effect.MysteryEffect;
import lk.apiit.ludot.effect.NoEffect;

/* One playing piece, for example R1.

   Two direction fields, not one: Rule T-5 puts a piece back to the direction
   it had when it first left the base, and Rule T-14 can change the current
   one, so the original has to be kept separately.

   The effect field is never null - NoEffect.INSTANCE is the Null Object, so
   nothing that reads it needs a null check. */
public class Piece {

    private final Colour colour;
    private final int number;          // 1..4

    private Position position;
    private Direction direction;
    private Direction originalDirection;
    private MysteryEffect effect = NoEffect.INSTANCE;
    private int captureCount;          // Rule T-7 needs at least one
    private int approachPasses;        // Rule T-1

    public Piece(Colour colour, int number) {
        if (number < 1 || number > GameRules.PIECES_PER_PLAYER) {
            throw new IllegalArgumentException("Piece number out of range: " + number);
        }
        this.colour = colour;
        this.number = number;
        this.position = Position.inBase(colour);
    }

    public Colour colour() {
        return colour;
    }

    /* "R1", "G3" - used all through the output messages. */
    public String name() {
        return colour.letter() + number;
    }

    public Position position() {
        return position;
    }

    public void moveTo(Position target) {
        this.position = target;
    }

    /* Rule 2 plus Rule T-1: the coin toss result is stored as BOTH
       directions, so Rule T-5 can restore it later. */
    public void enterBoard(int startIndex, Direction tossed) {
        this.position = Position.onPath(startIndex);
        this.direction = tossed;
        this.originalDirection = tossed;
    }

    public Direction direction() {
        return direction;
    }

    public void turnAround() {
        this.direction = direction.opposite();   // Rule T-14
    }

    public void restoreOriginalDirection() {
        this.direction = originalDirection;      // Rule T-5
    }

    public MysteryEffect effect() {
        return effect;
    }

    public void applyEffect(MysteryEffect effect) {
        this.effect = effect;
    }

    /* Called at the end of every round so timed effects can expire.
       The effect decides what it becomes next - no if-chain here. */
    public void endRound() {
        this.effect = effect.afterRound();
    }

    /* Rule T-13: the briefing effect counts threes, so every roll is
       reported to the effect, which returns whatever it becomes next. */
    public void noteRoll(int roll) {
        this.effect = effect.afterRoll(roll);
    }

    /* Rule T-13: three threes in a row sends the piece back to base. */
    public boolean mustReturnToBase() {
        return effect.sendsPieceToBase();
    }

    /* How far this piece actually travels for a dice value. */
    public int stepsFor(int roll) {
        return effect.adjustSteps(roll);
    }

    public boolean canMove() {
        return effect.allowsMovement();
    }

    public boolean hasCaptured() {
        return captureCount > 0;
    }

    public void recordCapture() {
        captureCount++;
    }

    /* Called by MovePieceCommand whenever a move crosses the approach cell. */
    public void notePassedApproach() {
        approachPasses++;
    }

    /* Rule T-1: a clockwise piece may turn in the first time it reaches its
       approach cell. A counterclockwise one has to go past it twice, which
       is what stops it reaching home a single step after leaving the base. */
    public boolean mayEnterHomeStraight() {
        return direction == Direction.CLOCKWISE || approachPasses >= 1;
    }

    public boolean isInBase() {
        return position.isInBase();
    }

    public boolean isHome() {
        return position.isHome();
    }

    /* Rule T-9: a captured piece loses everything, including its
       capture count and any mystery effect. */
    public void sendToBase() {
        this.position = Position.inBase(colour);
        this.direction = null;
        this.originalDirection = null;
        this.effect = NoEffect.INSTANCE;
        this.captureCount = 0;
        this.approachPasses = 0;
    }

    @Override
    public String toString() {
        return name();
    }
}
