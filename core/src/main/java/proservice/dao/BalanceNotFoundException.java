package proservice.dao;

/**
 * @author Mirian Dzhachvadze
 */
public class BalanceNotFoundException extends RuntimeException {
    public BalanceNotFoundException(String message) {
        super(message);
    }
}
