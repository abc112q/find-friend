package com.ariel.findfriendbackend.controller;

import com.ariel.findfriendbackend.common.BaseResponse;
import com.ariel.findfriendbackend.common.ErrorCode;
import com.ariel.findfriendbackend.common.ResultUtils;
import com.ariel.findfriendbackend.exception.BusinessException;
import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.model.request.UserLoginRequest;
import com.ariel.findfriendbackend.model.request.UserRegisterRequest;
import com.ariel.findfriendbackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

import static com.ariel.findfriendbackend.contant.UserConstant.ADMIN_ROLE;
import static com.ariel.findfriendbackend.contant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
//JSESSIONID=FABB8F806BF38E31F415EB3A3BEA195E
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    //@RequestParam注解用于将请求参数赋值给形参
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.getUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 通过传递不同参数选择查询哪些数据
     * mybatisplus分页
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request) {
        //如果有缓存，就直接读缓存，否则读db
        User loginUser=userService.getLoginUser(request);
        String redisKey =String.format("friend:user:recommend:%s",loginUser.getUserRole());
        ValueOperations<String,Object> valueOperations=redisTemplate.opsForValue();
        Page<User> userPage =(Page<User>) valueOperations.get(redisKey);
        if(userPage!=null){
            return ResultUtils.success(userPage);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //分页,当前是第几条数据公式：(pageNum-1)*pageSize
         userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        //写缓存,即使写失败了也可以通过读取数据库返回给页面，所以只需要捕获异常
        try{
            //这里设置一个统一的过期时间可能会出现缓存雪崩，记得解决一下
            valueOperations.set(redisKey,userPage,30000,TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error");
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 修改用户,需要获取当前用户信息request,并与登录的用户比较
     */
   @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //校验参数是否为空，校验是否有权限，更新
       if(user==null) {
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
       User loginUser=userService.getLoginUser(request);
       int result = userService.updateUser(user,loginUser);
       return ResultUtils.success(result);
   }


}
