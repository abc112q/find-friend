package com.ariel.findfriendbackend.service;

import com.ariel.findfriendbackend.model.domain.Tag;

import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.model.vo.TagVo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Ariel
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-04-24 16:30:04
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     *
     * @param tagNameList
     * @return
     */
    List<User> getUserByTags(List<String> tagNameList);

    /**
     * 利用数据库查询
     * @param tagNameList
     * @return
     */
    List<User> getUserByTagsSql(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断权限
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 重载方法
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取当前用户的标签
     * @param currentId
     * @param request
     * @return
     */
    TagVo getTags(String currentId, HttpServletRequest request);
}
