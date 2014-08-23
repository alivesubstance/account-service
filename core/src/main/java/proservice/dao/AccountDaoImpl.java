package proservice.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mirian Dzhachvadze
 */
public class AccountDaoImpl implements AccountDao {

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
            jdbcTemplate.update("insert into accounts values (?, ?)",
                    id, value);
        } else {
            jdbcTemplate.update("UPDATE accounts SET balance = ? WHERE id = ?",
                    currentAmount + value, id);
        }
    }
}
