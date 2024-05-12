package com.ariel.findfriendbackend;
import java.util.Date;

import com.ariel.findfriendbackend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void test(){
        //操作字符串
        ValueOperations valueOperations =redisTemplate.opsForValue();
        valueOperations.set("dog","dog1");
        valueOperations.set("cat",2);
        User user=new User();
        user.setId(1L);
        user.setUsername("fujia");
        valueOperations.set("USER",user);
        //取值
        Object dog=valueOperations.get("dog");
        Assertions.assertTrue("dog1".equals((String)dog));
        Object cat=valueOperations.get("cat");
        Assertions.assertTrue(2==(Integer)cat);
        System.out.println(valueOperations.get("USER"));
        redisTemplate.delete("cat");
    }
}
