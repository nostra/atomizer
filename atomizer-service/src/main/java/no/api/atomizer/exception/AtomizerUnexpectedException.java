package no.api.atomizer.exception;

/**
 * Something entirely not expected happened.
 */
public class AtomizerUnexpectedException extends AtomizerException {
    public AtomizerUnexpectedException(String message) {
        super(message);
    }
}
