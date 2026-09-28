package lk.apiit.ludot.command;

/* NULL OBJECT + SINGLETON.

   Notes 3g: "Return an object that describes the behaviour (or lack
   thereof) when null would normally be returned - by returning an object,
   no longer need to test or catch". Clean Code deck 2 slide 28 names the
   same idea: "Don't Return Null... use the Null object pattern".

   When no legal move exists the selector chain returns this instead of
   null, so the engine has no null check anywhere.

   An enum gives the SINGLETON for free. It is safe here precisely because
   this object holds no state at all - not even the player it was returned
   for. A singleton Board or Dice would make tests depend on each other,
   which is why neither of those is one. */
public enum NullMoveCommand implements GameCommand {

    INSTANCE;

    @Override
    public void execute() {
        // doing nothing is the whole point - the turn simply passes on
    }

    @Override
    public boolean isNoMove() {
        return true;
    }
}
