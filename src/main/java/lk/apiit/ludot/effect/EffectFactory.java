package lk.apiit.ludot.effect;

import java.util.Random;

/* FACTORY METHOD - Rule T-11.

   Notes 3e again: the parameter tells the method which kind of object to
   create. Here the destination decides the effect, so nothing else in the
   project has to know that Alpha means an aura and Beta means a briefing.

   The destination enum is nested because it has no meaning outside this
   factory and the mystery cell that uses it. */
public class EffectFactory {

    public enum Destination { ALPHA, BETA, GAMMA, BASE, START_X, APPROACH }

    private final Random random = new Random();

    /* Rule T-11: six equally likely destinations. */
    public Destination nextDestination() {
        Destination[] all = Destination.values();
        return all[random.nextInt(all.length)];
    }

    /* The lasting effect that comes with a destination. Three of the six
       leave nothing behind, and for those the Null Object is returned -
       never null. */
    public MysteryEffect effectFor(Destination destination) {
        return switch (destination) {
            case ALPHA -> new SpeedEffect(random.nextBoolean());  // Rule T-12
            case BETA -> new BriefingEffect();                    // Rule T-13
            case GAMMA, BASE, START_X, APPROACH -> NoEffect.INSTANCE;
        };
    }
}
