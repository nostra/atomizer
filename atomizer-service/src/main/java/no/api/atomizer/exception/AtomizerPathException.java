package no.api.atomizer.exception;

/**
 * Something went wrong in the web controller on the path the user has given.
 * This usually indicate an error in usage.
 */
public class AtomizerPathException extends AtomizerException {

    public AtomizerPathException(String message) {
        super(message);
    }
}
