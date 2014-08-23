package proservice.dao;

/**
 * @author Mirian Dzhachvadze
 */
public interface AccountDao {
    Long getAmount(Integer id);

    void addAmount(Integer id, Long value);
}
