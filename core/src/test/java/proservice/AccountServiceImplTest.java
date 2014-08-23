package proservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import proservice.cache.CacheServiceException;
import proservice.dao.BalanceNotFoundException;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@ContextConfiguration(locations = "classpath:core-context-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void testInOneThread() throws Exception {
        int id = 800;
        long value = 2L;
        accountService.addAmount(id, value);

        assertEquals(accountService.getAmount(id).longValue(), value);
    }

    @Test
    public void testInMultiThreads() throws Exception {
        final int readers = 20;
        final int writers = 10;

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(readers + writers);

        for (int i = 0; i < writers; i++) {
            final int num = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startSignal.await();

                        accountService.addAmount(num, (long) num);
                        System.out.println("Writer [" + num + "] put [" + num + "]");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }
            }).start();
        }

        for (int i = 0; i < readers; i++) {
            final int num = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startSignal.await();

                        Long amount = accountService.getAmount(num);
                        if (amount != null) {
                            assertEquals(amount.longValue(), num);
                        }

                        System.out.println("Reader [" + num + "] read [" + amount + "]");
                    } catch (CacheServiceException e) {
                        System.out.println("Failed to fetch balance from account [" + num + "]");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }
            }).start();
        }

        startSignal.countDown();

        doneSignal.await();
    }

}