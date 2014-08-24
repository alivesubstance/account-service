package proservice.dao;

import java.util.Map;

/**
 * @author Mirian Dzhachvadze
 */
public interface AccountDao {
    Long getAmount(Integer id);

    void addAmount(Integer id, Long value);

    Map<Integer, Long> getAll();
}
