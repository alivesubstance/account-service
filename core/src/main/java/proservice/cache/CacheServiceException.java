package proservice.cache;

/**
 * @author Mirian Dzhachvadze
 */
public class CacheServiceException extends RuntimeException {
    public CacheServiceException(String message) {
        super(message);
    }

    public CacheServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheServiceException(Throwable cause) {
        super(cause);
    }
}
