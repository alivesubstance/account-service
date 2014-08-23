package proservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import proservice.cache.CacheService;
import proservice.cache.InMemoryCacheService;
import proservice.dao.AccountDaoImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirian Dzhachvadze
 */
public class ConcurrentAccountService implements AccountService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConcurrentAccountService.class);

    private final CacheService cacheService;

    public ConcurrentAccountService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Long getAmount(Integer id) {
        LOGGER.info("ConcurrentAccountService.getAmount - "
                    + "Request balance for account [" + id + "]");

        return cacheService.getAmount(id);
    }

    @Override
    public void addAmount(Integer id, Long value) {
        LOGGER.info("ConcurrentAccountService.addAmount - "
                    + "Add amount [" + value + "] to account [" + id + "]");

        cacheService.addAmount(id, value);
    }
}
