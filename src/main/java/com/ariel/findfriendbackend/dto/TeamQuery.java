package com.ariel.findfriendbackend.dto;

import com.ariel.findfriendbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 队伍查询封装类
 * @author Ariel
 * 为什么需要请求参数包装类？
 * 请求参数名称与实体类不一样，实体类有些参数用不到，会增加理解成本
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
