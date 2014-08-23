package proservice.cache;

/**
 * @author Mirian Dzhachvadze
 */
public interface CacheService {

    Long getAmount(Integer id);

    void addAmount(Integer id, Long value);

}
