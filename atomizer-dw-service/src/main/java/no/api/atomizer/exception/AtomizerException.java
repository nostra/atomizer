package no.api.atomizer.exception;

/**
 *
 */
public class AtomizerException extends RuntimeException {

    public AtomizerException(String s) {
        super(s);
    }

    public AtomizerException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
