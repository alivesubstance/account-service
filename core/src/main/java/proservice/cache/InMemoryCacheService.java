package proservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proservice.dao.AccountDao;
import proservice.dao.AccountDaoImpl;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Mirian Dzhachvadze
 */
//TODO try with AtomicLong.compareAndSet
public class InMemoryCacheService implements CacheService {

    private final static Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheService.class);

    private final AccountDao accountDao;
    private final Cache<Integer, Long> cache;

    private final ConcurrentMap<Integer, Object> idsLocks = Maps.newConcurrentMap();
    private final Object lock = new Object();

    public InMemoryCacheService(AccountDao accountDao) {
        this.accountDao = accountDao;
        cache = CacheBuilder.newBuilder()
                //TODO play with concurrency level
                //.concurrencyLevel()
                .recordStats().build();
    }

    @Override
    public Long getAmount(final Integer id) {
        try {
            return cache.get(id, new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return accountDao.getAmount(id);
                }
            });
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
                    currentValue += value;
                } else {
                    currentValue = value;
                }
                cache.put(id, currentValue);
                accountDao.addAmount(id, currentValue);
            }
        } catch (Exception e) {
            // remove balance from cache in case of any errors with data base
            cache.invalidate(id);

            throw new CacheServiceException("Failed to add amount for for balance id [" + id + "]");
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
}
