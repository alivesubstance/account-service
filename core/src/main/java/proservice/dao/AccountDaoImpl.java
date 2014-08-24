package proservice.dao;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
        //TODO think to make it in one query
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

    @Override
    public Map<Integer, Long> getAll() {
        return jdbcTemplate.query("select * from accounts", new ResultSetExtractor<Map<Integer, Long>>() {
            @Override
            public Map<Integer, Long> extractData(ResultSet rs)
                    throws SQLException, DataAccessException {
                Map<Integer, Long> balances = Maps.newHashMap();
                while (rs.next()) {
                    balances.put(rs.getInt(1), rs.getLong(2));
                }

                return balances;
            }
        });
    }
}
