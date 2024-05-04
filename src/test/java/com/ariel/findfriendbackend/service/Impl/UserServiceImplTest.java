package com.ariel.findfriendbackend.service.Impl;

import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    public void getUserByTags() {
        //创造标签列表
        List<String> tagNameList = Arrays.asList("java","python");
        List<User> userList =userService.getUserByTags(tagNameList);
        for(User list:userList){
            System.out.println(list.toString());
        }
        Assert.assertNotNull(userList); //断言得到的userList不是空
    }
}