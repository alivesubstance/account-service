package proservice;

import java.util.concurrent.Callable;

/**
 * @author Mirian Dzhachvadze
 */
public class Writer implements Callable<Long> {
    private final HttpClientWrapper httpClientWrapper;
    private final Integer id;
    private final Long value;

    public Writer(HttpClientWrapper httpClientWrapper, Integer id, Long value) {
        this.httpClientWrapper = httpClientWrapper;
        this.id = id;
        this.value = value;
    }

    @Override
    public Long call() {
        httpClientWrapper.addAmount(id, value);
        return null;
    }
}
