package proservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import proservice.dao.AccountDao;
import proservice.dao.AccountDaoImpl;
import proservice.dao.BalanceNotFoundException;
import proservice.stats.StatsEvent;
import proservice.stats.StatsTracker;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Mirian Dzhachvadze
 */
public class InMemoryCacheService implements CacheService, InitializingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheService.class);

    private final AccountDao accountDao;

    private final Cache<Integer, Long> cache;
    private final ConcurrentMap<Integer, Object> idsLocks = Maps.newConcurrentMap();

    private final Object lock = new Object();

    public InMemoryCacheService(final AccountDao accountDao, Long maxCacheValues,
            Integer concurrencyLevel) {
        this.accountDao = accountDao;
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .maximumSize(maxCacheValues)
                .recordStats()
                .build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // pre-populate cache
        Map<Integer, Long> balances = accountDao.getAll();
        cache.putAll(balances);

        LOGGER.info("InMemoryCacheService.afterPropertiesSet - "
                    + "Cache pre-populated with [" + balances.size() + "] balances");
    }

    @Override
    public Long getAmount(final Integer id) {
        try {
            final MutualBoolean isCacheUsed = new MutualBoolean(true);
            Long value = cache.get(id, new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    isCacheUsed.value = false;
                    return accountDao.getAmount(id);
                }
            });

            if (isCacheUsed.value) {
                LOGGER.info("InMemoryCacheService.getAmount - "
                            + "Load value [" + value + "] for balance [" + id + "] from cache");
            }

            return value;
        } catch (Exception e) {
            throw new CacheServiceException(e);
        }
    }

    @Override
    public void addAmount(Integer id, Long value) {
        try {
            synchronized (getIdLock(id)) {
                Long currentValue = cache.getIfPresent(id);
                if (currentValue != null) {
                    value += currentValue;
                }

                LOGGER.info("InMemoryCacheService.addAmount - " +
                            "Put value [" + value + "] to cache for balance [" + id + "]");
                cache.put(id, value);
                accountDao.addAmount(id, value);
            }
        } catch (Exception e) {
            // remove balance from cache in case of any errors with data base
            cache.invalidate(id);

            throw new CacheServiceException("Failed to add value for balance id [" + id + "]");
        } finally {
            releaseIdLock(id);
        }
    }

    private Object getIdLock(Integer id) {
        Object lockForId = idsLocks.get(id);

        if (lockForId == null) {
            synchronized (lock) {
                if (!idsLocks.containsKey(id)) {
                    idsLocks.put(id, id);
                }

                lockForId = idsLocks.get(id);
            }
        }

        if (lockForId == null) {
            LOGGER.warn("InMemoryCacheService.getIdLock - Lock type NULL for ID [" + id + "]");
            lockForId = id;
        }

        return lockForId;
    }


    private void releaseIdLock(Integer id) {
        idsLocks.remove(id);
    }

    private class MutualBoolean {
        boolean value;

        public MutualBoolean(boolean b) {
            this.value = b;
        }
    }
}
