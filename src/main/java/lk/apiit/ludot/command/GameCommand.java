package lk.apiit.ludot.command;

/* COMMAND - the interface.

   Notes 3f: the motivation is that a request receiver "often requires a
   large selection statement to work out the appropriate response". Without
   this, the turn loop would be one long if/else over move types.

   Each move becomes an object that knows how to carry itself out, so the
   engine just calls execute() without knowing what kind of move it is. */
public interface GameCommand {

    void execute();

    /* Rule 4 and Rule T-2: a six or a capture earns another roll. The
       command knows which happened, so it answers rather than the engine
       having to inspect the board afterwards. */
    default boolean grantsExtraRoll() {
        return false;
    }

    /* Lets the engine print the brief's "no legal move" message. The
       Null Object answers true; every real command keeps this default.
       The engine asks the command - it never compares anything to null. */
    default boolean isNoMove() {
        return false;
    }
}
