package proservice;

import java.util.concurrent.Callable;

/**
 * @author Mirian Dzhachvadze
 */
public class Reader implements Callable<Long> {
    private final HttpClientWrapper httpClientWrapper;
    private final Integer id;

    public Reader(HttpClientWrapper httpClientWrapper, Integer id) {
        this.httpClientWrapper = httpClientWrapper;
        this.id = id;
    }

    @Override
    public Long call() throws Exception {
        return httpClientWrapper.getAmount(id);
    }
}
