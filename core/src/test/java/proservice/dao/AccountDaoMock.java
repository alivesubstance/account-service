package proservice.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirian Dzhachvadze
 */
//@NotThreadSafe
public class AccountDaoMock implements AccountDao {

    private Map<Integer, Long> map = new HashMap<Integer, Long>();

    @Override
    public Long getAmount(Integer id) {
        Long value = map.get(id);
        if (value == null) {
            throw new BalanceNotFoundException("Failed to find balance id [" + id + "]");
        }
        return value;
    }

    @Override
    public void addAmount(Integer id, Long value) {
        Long currentValue = getAmount(id);
        map.put(id, value + currentValue);
    }
}
