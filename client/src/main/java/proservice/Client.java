package proservice;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Mirian Dzhachvadze
 */
public class Client {

    private final static Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final static Map<Integer, Long> balances = Maps.newConcurrentMap();
    private static boolean assertBalances = false;

    public static void main(String[] args) {
        int rCount = Integer.parseInt(getProperty("rCount"));
        int wCount = Integer.parseInt(getProperty("wCount"));
        int activeThreads = Integer.parseInt(getProperty("activeThreads", "-1"));
        List<Integer> idList = createIdList(getProperty("idList"));

        HttpClientWrapper httpClientWrapper = getHttpClientWrapper();

        if (assertBalances) {
            readGivenAccountBalances(httpClientWrapper, idList);
        }

        AccountExecutorService accountExec = new AccountExecutorService(
                httpClientWrapper, rCount, wCount, idList, activeThreads);
        accountExec.start();

        if (assertBalances) {
            doAssertBalances(httpClientWrapper, idList);
        }

        httpClientWrapper.close();
        accountExec.shutDown();
    }

    private static String getProperty(String key) {
        String property = System.getProperty(key);
        if (StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException("Failed to find property [" + key + "]");
        }

        return property;
    }

    private static String getProperty(String key, String defaultProperty) {
        try {
            return getProperty(key);
        } catch (IllegalArgumentException e) {
            return defaultProperty;
        }
    }

    private static void readGivenAccountBalances(HttpClientWrapper httpClientWrapper,
            List<Integer> idList) {
        LOGGER.info("Client.readGivenAccountBalances - "
                    + "Read all given balances " + idList);

        for (Integer id : idList) {
            registerBalanceValue(id, httpClientWrapper.getAmount(id));
        }
    }

    private static void doAssertBalances(HttpClientWrapper httpClientWrapper, List<Integer> idList) {
        for (Integer id : idList) {
            Long actual = httpClientWrapper.getAmount(id);
            Long expected = balances.get(id);
            if (expected != null) {
                String msg = "Result balance [" + id + "] has wrong value: " +
                "expected [" + expected + "], actual [" + actual + "]";

                Preconditions.checkArgument(actual.equals(expected), msg);
            }
        }
    }

    private static HttpClientWrapper getHttpClientWrapper() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "client-context.xml");
        HttpClientWrapper httpClientWrapper = applicationContext.getBean("httpClientWrapper",
                HttpClientWrapper.class);

        Preconditions.checkNotNull(httpClientWrapper);

        return httpClientWrapper;
    }

    private static List<Integer> createIdList(String ids) {
        List<Integer> idList = Lists.newArrayList();
        StringTokenizer tokenizer = new StringTokenizer(ids, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.contains("-")) {
                String[] interval = token.split("-");
                for (int i = Integer.parseInt(interval[0]); i <= Integer.parseInt(interval[1]); i++) {
                    idList.add(i);
                }
            } else {
                idList.add(Integer.valueOf(token));
            }
        }

        return idList;
    }

    // register balance changes for assertion purpose
    public static void registerBalanceValue(Integer id, Long value) {
        if (assertBalances && value != null) {
            LOGGER.info("Client.registerBalanceValue - "
                        + "Register value [" + value + "] for balance [" + id + "]");

            Long currentValue = balances.get(id);
            if (currentValue != null) {
                value += currentValue;
            }
            balances.put(id, value);
        }
    }
}
