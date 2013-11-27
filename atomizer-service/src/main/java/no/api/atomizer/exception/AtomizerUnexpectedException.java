package no.api.atomizer.exception;

/**
 * Something entirely not expected happened.
 */
public class AtomizerUnexpectedException extends RuntimeException {
    public AtomizerUnexpectedException(String message) {
        super(message);
    }
}
