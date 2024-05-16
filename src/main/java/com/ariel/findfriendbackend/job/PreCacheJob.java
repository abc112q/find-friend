package com.ariel.findfriendbackend.job;

import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ariel
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 重点用户，只设置一个用户
     */
    private List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 每天执行，预热推荐用户
     * 参数：s min h 日 月 年
     */
    @Scheduled(cron ="0 10 17 * * *")
    public void doCacheRecommendUser(){
        RLock lock=redissonClient.getLock("friend:preCacheJob:docache:lock");
        /**
         *  参数1：等待时间（0，每天只执行一个定时任务(只有一个任务能拿到锁)，要是没拿到锁就直接走等第二天）
         *  参数2：lock的过期时间
         */
        try {
            //如果获取到锁就执行定时任务写缓存
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock: "+Thread.currentThread().getId());
                for(Long userId:mainUserList){
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    //分页,当前是第几条数据公式：(pageNum-1)*pageSize
                    Page<User> userPage = userService.page(new Page<>(1,20),queryWrapper);
                    String redisKey =String.format("friend:user:recommend:%s",userId);
                    ValueOperations<String,Object> valueOperations=redisTemplate.opsForValue();
                    //写缓存
                    try{
                        valueOperations.set(redisKey,userPage,30000L, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doPreCacheError",e);
        }finally {
            // redisson自己提供了一个看门狗机制，为我们提供了一个续期机制，不需要我们自己写，条件是leaseTime必须设置为-1
            //如果放到try中，如果前边有报错，释放锁这里就不会执行。
            //判断释放这个锁的条件是：当前锁是当前线程设置的
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
                System.out.println("releaseLock: "+Thread.currentThread().getId());
            }
        }
    }
}
