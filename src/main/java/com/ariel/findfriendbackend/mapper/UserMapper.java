package com.ariel.findfriendbackend.mapper;

import com.ariel.findfriendbackend.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
* @author Ariel
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-04-24 16:30:04
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




