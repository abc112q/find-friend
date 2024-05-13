package com.ariel.findfriendbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 *
 * @author Ariel
 */
@Data

public class PageRequest implements Serializable {

    /**
     * 页面大小
     */
    protected int pageSize =10;
    /**
     * 当前是第几页
     */
    protected int pageNum =1;
}
