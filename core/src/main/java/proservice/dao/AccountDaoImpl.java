package proservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mirian Dzhachvadze
 */
public class AccountDaoImpl implements AccountDao {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountDaoImpl.class);
    private JdbcTemplate jdbcTemplate;

    // for CGLIB needs
    public AccountDaoImpl() {
    }

    public AccountDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Override
    public Long getAmount(Integer id) {
        try {
            LOGGER.info("AccountDaoImpl.addAmount - "
                        + "Select value for balance [" + id + "]");
            return jdbcTemplate.queryForObject("SELECT balance FROM accounts WHERE id = ?",
                    new Object[]{id}, Long.class);
        }
        catch (EmptyResultDataAccessException e) {
            throw new BalanceNotFoundException("Failed to find balance id [" + id + "]");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAmount(Integer id, Long value) {
        Long currentAmount;
        try {
            currentAmount = getAmount(id);
        } catch (BalanceNotFoundException e) {
            currentAmount = null;
        }

        if (currentAmount == null) {
            LOGGER.info("AccountDaoImpl.addAmount - "
                        + "Insert value [" + value + "] for balance [" + id + "]");
            jdbcTemplate.update("insert into accounts values (?, ?)",
                    id, value);
        } else {
            LOGGER.info("AccountDaoImpl.addAmount - "
                        + "Update value [" + value + "] for balance [" + id + "]");
            jdbcTemplate.update("UPDATE accounts SET balance = ? WHERE id = ?",
                    currentAmount + value, id);
        }
    }
}
