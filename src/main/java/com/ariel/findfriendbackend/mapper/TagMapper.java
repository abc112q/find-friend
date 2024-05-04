package com.ariel.findfriendbackend.mapper;

import com.ariel.findfriendbackend.model.domain.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Ariel
* @description 针对表【tag(用户)】的数据库操作Mapper
* @createDate 2024-04-24 16:29:29
* @Entity generator.domain.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




