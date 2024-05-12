package com.ariel.findfriendbackend.service;

import com.ariel.findfriendbackend.FindFriendBackendApplication;
import com.ariel.findfriendbackend.model.domain.User;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import javafx.application.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FindFriendBackendApplication.class})
public class InsertUsersTest {
    @Autowired
    private UserService userService;

    /**
     * 批量插入数据
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM =1000000;
        List<User> userList =new ArrayList<>();
        for(int i=0;i<INSERT_NUM;i++){
            User user=new User();
            user.setUsername("测试用户");
            user.setUserAccount("test");
            user.setAvatarUrl("https://picx.zhimg.com/v2-a2947e4762bb8badb9421423621435e6_r.jpg?source=1def8aca");
            user.setGender(2);
            user.setUserPassword("12345678");
            user.setPhone("111111111");
            user.setEmail("22222222@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("['test']");
            user.setProfile("测试用户");
            userList.add(user);
        }
        //修改batchSize可能速度更快，且目前为止是顺序插入
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     *自定义线程池的参数是高频考点，以及什么时候才会使用最大线程数等等问题
     * 后边还有一个参数是策略，可以默认中断或者自定义策略打日志。
     * 第一个参数是核心线程数，这个数量可以根据看任务是cpu密集还是io密集
     */

    private ExecutorService executorService =new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    /**
     * 多线程并发插入数据
     * 把10w条分成10组，每组1w条
     */
    @Test
    public void doConcurrentInsertUsers(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        int batchSize =1000;
        //分10组
        int j=0;
        List<CompletableFuture<Void>> futureList =new ArrayList<>();
        for(int i=0;i<100;i++){
            List<User> userList = Collections.synchronizedList(new ArrayList<>());

            while(true){
                j++;
                User user=new User();
                user.setUsername("测试用户");
                user.setUserAccount("test4");
                user.setAvatarUrl("https://picx.zhimg.com/v2-a2947e4762bb8badb9421423621435e6_r.jpg?source=1def8aca");
                user.setGender(2);
                user.setUserPassword("12345678");
                user.setPhone("111111111");
                user.setEmail("22222222@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("test4");
                user.setProfile("测试用户第4批");
                userList.add(user);
                if(j%batchSize==0){
                    break;
                }
            }
            //创建一个异步的任务,前提是对数据插入的顺序无所谓
            //如果要在异步中实现上述while（）中的插入数据，就不能使用list集合要使用线程安全的集合
            CompletableFuture<Void> future =CompletableFuture.runAsync(()->{
                System.out.println("threadName"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
