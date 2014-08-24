package proservice.stats;

import com.google.common.collect.Lists;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author Mirian Dzhachvadze
 */
public class StatsEvent {
    private final StatsEventType type;
    private final List<Long> events = Lists.newLinkedList();
    private long firstTimeStamp;
    private int totalEvents;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    public StatsEvent(StatsEventType type) {
        this.type = type;
    }

    public void register(long timestamp) {
        if (firstTimeStamp == 0) {
            firstTimeStamp = timestamp;
        }
        events.add(timestamp);
        totalEvents++;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    @Override
    public String toString() {
        StringBuilder rez = new StringBuilder();
        if (!events.isEmpty()) {
            long rate = StatsTracker.RATE_PERIOD_MS;
            rez.append("[");
            rez.append(type.name());
            rez.append(" {");
            rez.append(System.currentTimeMillis() - firstTimeStamp);
            rez.append(":");
            rez.append(events.size());
            rez.append("}, ");
            rez.append(calculateRate(rate));
            rez.append("]");
        }

        return rez.toString();
    }

    private String calculateRate(long requiredPeriodMs) {
        int maxRate = 0;
        int rate = 1;
        long calculatedPeriod = 0;
        long firstEvent = events.get(0);
        for (int i = 1; i < events.size(); i++) {
            Long timestamp = events.get(i);
            calculatedPeriod = timestamp - firstEvent;
            if (calculatedPeriod < requiredPeriodMs) {
                rate++;
            } else {
                if (rate > maxRate) {
                    maxRate = rate;
                }
                firstEvent = timestamp;
            }
        }

        // in case we don't exceed requiredPeriodMs
        maxRate = rate > maxRate ? rate : maxRate;

        String formattedPeriod = decimalFormat.format((double) calculatedPeriod / 1e3);
        return maxRate + " per " + formattedPeriod + "s";
    }
}
