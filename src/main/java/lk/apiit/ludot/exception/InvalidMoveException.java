package lk.apiit.ludot.exception;

/* Custom unchecked exception.

   Clean Code deck 2, slide 16: "Use Unchecked Exceptions" - a checked one
   would force a throws clause through every layer from the command up to
   the facade. Slide 17: "Use Custom Exceptions" - a named type says what
   went wrong far better than IllegalArgumentException does. */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
