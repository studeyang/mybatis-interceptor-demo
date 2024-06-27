package com.oujiong.mapper;

import com.oujiong.entity.TabUser;
import com.oujiong.plugin.ReadEncryptInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2024/6/27/027
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReadPressureTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReadEncryptInterceptor readEncryptInterceptor;

    private Executor executor;

    private static final Integer TIMES = 100;

    @Before
    public void setUp() {
        executor = new ThreadPoolExecutor(5, 5, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(TIMES));
    }

    private Runnable getTask(boolean print) {
        return () -> {
            TabUser user = userMapper.selectByPrimaryKey(679403741908963328L);
            if (print) {
                System.out.println(Thread.currentThread().getName() + ": " + user);
            }
        };
    }

    @Test
    public void selectByPrimaryKey_pool() throws ExecutionException, InterruptedException {

        // 1. 预热
        List<CompletableFuture<Void>> preheat = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(getTask(false), executor);
            preheat.add(task);
        }
        for (CompletableFuture<Void> task : preheat) {
            task.get();
        }

        StopWatch stopWatch = new StopWatch();

        // 2. 有加解密拦截器
        stopWatch.start("有加解密拦截器");
        List<CompletableFuture<Void>> encryptTasks = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(getTask(false), executor);
            encryptTasks.add(task);
        }
        for (CompletableFuture<Void> encryptTask : encryptTasks) {
            encryptTask.get();
        }
        stopWatch.stop();

        // 3. 关闭拦截
        getTask(true).run();
        readEncryptInterceptor.off();
        getTask(true).run();

        // 4. 无加解密拦截器
        stopWatch.start("无加解密拦截器");
        List<CompletableFuture<Void>> originTasks = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            CompletableFuture<Void> task = CompletableFuture.runAsync(getTask(false), executor);
            originTasks.add(task);
        }
        for (CompletableFuture<Void> originTask : originTasks) {
            originTask.get();
        }
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    public void selectByPrimaryKey_thread() throws InterruptedException {

        // 1. 预热
        List<Thread> preheat = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            preheat.add(new Thread(getTask(false)));
        }
        for (int i = 0; i < TIMES; i++) {
            preheat.get(i).start();
            preheat.get(i).join();
        }

        // 2. 任务准备
        List<Thread> tasks = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            tasks.add(new Thread(getTask(false)));
        }
        StopWatch stopWatch = new StopWatch();

        // 3. 有加解密拦截器
        stopWatch.start("有加解密拦截器");
        for (int i = 0; i < TIMES; i++) {
            if (i % 2 == 0) {
                tasks.get(i).start();
                tasks.get(i).join();
            }
        }
        stopWatch.stop();

        // 4. 关闭拦截
        getTask(true).run();
        readEncryptInterceptor.off();
        getTask(true).run();

        // 5. 无加解密拦截器
        stopWatch.start("无加解密拦截器");
        for (int i = 0; i < TIMES; i++) {
            if (i % 2 != 0) {
                tasks.get(i).start();
                tasks.get(i).join();
            }
        }
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

}