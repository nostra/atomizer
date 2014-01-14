package no.api.atomizer.cachechannel;

/**
 *
 */
public class CacheChannelException extends RuntimeException {

    public CacheChannelException(String s) {
        super(s);
    }

    public CacheChannelException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
