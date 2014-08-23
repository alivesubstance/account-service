package proservice;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Mirian Dzhachvadze
 */
public class Client {

    private final static Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final static Map<Integer, Long> balances = Maps.newConcurrentMap();
    private final static boolean assertBalances = false;

    public static void main(String[] args) {
        int rCount = 1;
        int wCount = 0;
        Integer[] idList = createIdList("1,2-5");
        HttpClientWrapper httpClientWrapper = getHttpClientWrapper();

        if (assertBalances) {
            readGivenAccountBalances(httpClientWrapper, idList);
        }

        new AccountExecutorService(httpClientWrapper, rCount, wCount, idList, -1).start();

        if (assertBalances) {
            doAssertBalances(httpClientWrapper, idList);
        }

        httpClientWrapper.close();
    }

    private static void readGivenAccountBalances(HttpClientWrapper httpClientWrapper,
            Integer[] idList) {
        LOGGER.info("Client.readGivenAccountBalances - "
                    + "Read all given balances " + Arrays.toString(idList));

        for (Integer id : idList) {
            registerBalanceValue(id, httpClientWrapper.getAmount(id));
        }
    }

    private static void doAssertBalances(HttpClientWrapper httpClientWrapper, Integer[] idList) {
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

    private static Integer[] createIdList(String ids) {
        return new Integer[]{1};

        //StringTokenizer tokenizer = new StringTokenizer(ids, ",");
        //while (tokenizer.nextToken()) {
        //
        //}
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
