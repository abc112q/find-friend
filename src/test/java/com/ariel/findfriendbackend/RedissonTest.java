package com.ariel.findfriendbackend;

import com.ariel.findfriendbackend.model.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    void test(){
        //java list 数据存在本地JVM内存中
        List<String> list=new ArrayList<>();
        list.add("a");
        list.get(0);
        System.out.println("list:"+list.get(0));
        //list.remove(0);

        //redisson 存在redis中,操作跟list一样
        RList<String> rlist= redissonClient.getList("test-rlist");
        rlist.add("b");
        rlist.get(0);
        //rlist.remove(0);
        System.out.println("rlist:"+rlist.get(0));
    }

    /**
     * 测试redisson看门狗
     */
    @Test
    void testDog(){
        RLock lock=redissonClient.getLock("friend:preCacheJob:docache:lock");
        try {
            //如果获取到锁就执行定时任务写缓存
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                //如果使用debug看是否会自动续期，会被redisson当成服务宕机，所以让他sleep
                Thread.sleep(300000);
                System.out.println("getLock: "+Thread.currentThread().getId());

            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }finally {

            if(lock.isHeldByCurrentThread()){
                System.out.println("releaseLock: "+Thread.currentThread().getId());
                lock.unlock();

            }
        }
    }
}

