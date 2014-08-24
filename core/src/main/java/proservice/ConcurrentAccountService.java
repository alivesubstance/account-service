package proservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import proservice.cache.CacheService;
import proservice.cache.InMemoryCacheService;
import proservice.dao.AccountDaoImpl;
import proservice.stats.StatsEventType;
import proservice.stats.StatsTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirian Dzhachvadze
 */
public class ConcurrentAccountService implements AccountService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConcurrentAccountService.class);

    private final CacheService cacheService;
    private final StatsTracker statsTracker = StatsTracker.getInstance();

    public ConcurrentAccountService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Long getAmount(Integer id) throws AccountServiceException {
        statsTracker.publish(StatsEventType.GET_AMOUNT);

        LOGGER.info("ConcurrentAccountService.getAmount - "
                    + "Request balance [" + id + "]");

        try {
            return cacheService.getAmount(id);
        } catch (Exception e) {
            String msg = "Failed to get value for balance [" + id + "]";
            LOGGER.error("ConcurrentAccountService.getAmount - " + msg, e);
            throw new AccountServiceException(msg);
        }
    }

    @Override
    public void addAmount(Integer id, Long value) throws AccountServiceException {
        statsTracker.publish(StatsEventType.ADD_AMOUNT);

        LOGGER.info("ConcurrentAccountService.addAmount - "
                    + "Add amount [" + value + "] to balance [" + id + "]");

        try {
            cacheService.addAmount(id, value);
        } catch (Exception e) {
            String msg = "Failed to add value for balance [" + id + "]";
            LOGGER.error("ConcurrentAccountService.addAmount - " + msg, e);
            throw new AccountServiceException(msg);
        }
    }
}
