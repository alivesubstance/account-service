package proservice;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.http.client.HttpClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;

/**
 * @author Mirian Dzhachvadze
 */
public class Client {
    public static void main(String[] args) {
        int rCount = 10;
        int wCount = 10;
        Integer[] idList = createIdList("1,2");

        HttpClientWrapper httpClientWrapper = getHttpClientWrapper();

        new AccountExecutorService(httpClientWrapper, rCount, wCount, idList, -1).start();

        httpClientWrapper.close();
    }

    private static HttpClientWrapper getHttpClientWrapper() {
        ClassPathXmlApplicationContext applicationContext
                = new ClassPathXmlApplicationContext("client-context.xml");
        HttpClientWrapper httpClientWrapper = applicationContext.getBean("httpClientWrapper",
                HttpClientWrapper.class);

        Preconditions.checkNotNull(httpClientWrapper);

        return httpClientWrapper;
    }

    private static Integer[] createIdList(String ids) {
        return new Integer[]{2};
    }
}
