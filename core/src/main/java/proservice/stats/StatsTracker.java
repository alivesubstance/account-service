package proservice.stats;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mirian Dzhachvadze
 */
public class StatsTracker {

    private final static Logger LOGGER = LoggerFactory.getLogger(StatsTracker.class);

    private final static StatsTracker INSTANCE = new StatsTracker();
    private final static long PERIOD = 30 * 1000;

    // calculate max events number for a give period
    public final static long RATE_PERIOD_MS = 1000;

    private final Map<StatsEventType, StatsEvent> stats = Maps.newHashMap();
    private final ReentrantLock lock = new ReentrantLock();

    private StatsTracker() {
        Timer timer = new Timer("STATS_TRACKER", true);
        timer.schedule(new StatsLogger(), PERIOD, PERIOD);
    }

    public static StatsTracker getInstance() {
        return INSTANCE;
    }

    public void publish(StatsEventType type) {
        lock.lock();
        try {
            StatsEvent statsEvent = stats.get(type);
            if (statsEvent == null) {
                statsEvent = new StatsEvent(type);
                stats.put(type, statsEvent);
            }
            statsEvent.register(System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reset stats
     */
    public void clear() {
        stats.clear();
    }

    private class StatsLogger extends TimerTask {
        @Override
        public void run() {
            lock.lock();
            try {
                printStats();
                clear();
            } finally {
                lock.unlock();
            }
        }

        private void printStats() {
            StringBuilder sb = new StringBuilder();
            Iterator<StatsEventType> it = stats.keySet().iterator();
            int total = 0;
            while (it.hasNext()) {
                StatsEvent statsEvent = stats.get(it.next());
                sb.append(statsEvent);

                if (it.hasNext()) {
                    sb.append(", ");
                }

                total += statsEvent.getTotalEvents();
            }

            if (sb.length() > 0) {
                sb.append(" TOTAL ");
                sb.append(total);

                LOGGER.info("StatsLogger.printStats - " + sb);
            }
        }
    }
}
