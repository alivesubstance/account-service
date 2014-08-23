package proservice.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:dao-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountDaoImplIT {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private BasicDataSource dataSource;

    @Before
    public void setUp() throws Exception {
        // let @Rollback do it job
        dataSource.setDefaultAutoCommit(false);
    }

    @Rollback
    @Test
    public void test() throws Exception {
        int id = 1;
        long value = 2L;

        accountDao.addAmount(id, value);

        assertEquals(accountDao.getAmount(id).longValue(), value);
    }
}