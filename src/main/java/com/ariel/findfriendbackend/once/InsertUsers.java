package com.ariel.findfriendbackend.once;


import com.ariel.findfriendbackend.mapper.UserMapper;
import com.ariel.findfriendbackend.model.domain.User;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;
    /**
     * 批量插入用户
     * 用stopwatch计时，看插入数据需要多少时间
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM =1000;
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
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
