package lk.apiit.ludot.effect;

/* NULL OBJECT + SINGLETON - "this piece has no mystery effect".

   Notes 3g: by returning an object rather than null, callers no longer
   need to test. Every Piece starts with this one, so Piece.stepsFor() and
   Piece.canMove() work from the first turn without a single null check.

   An enum constant is the simplest correct singleton in Java, and it is
   safe here only because this object has no mutable state at all. */
public enum NoEffect implements MysteryEffect {

    INSTANCE;

    @Override
    public int adjustSteps(int roll) {
        return roll;                    // unchanged
    }

    @Override
    public boolean allowsMovement() {
        return true;
    }

    @Override
    public MysteryEffect afterRoll(int roll) {
        return this;                    // nothing to count
    }

    @Override
    public MysteryEffect afterRound() {
        return this;                    // never expires
    }

    @Override
    public boolean sendsPieceToBase() {
        return false;
    }

    @Override
    public String describe() {
        return "normal";
    }
}
