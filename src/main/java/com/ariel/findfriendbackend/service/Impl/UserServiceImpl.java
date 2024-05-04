package com.ariel.findfriendbackend.service.Impl;
import com.ariel.findfriendbackend.common.ErrorCode;
import com.ariel.findfriendbackend.exception.BusinessException;
import com.ariel.findfriendbackend.mapper.UserMapper;
import com.ariel.findfriendbackend.model.domain.Tag;
import com.ariel.findfriendbackend.model.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ariel.findfriendbackend.service.UserService;

import static com.ariel.findfriendbackend.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author Ariel
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-04-24 16:30:04
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "ariel";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复,查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setProfile(originUser.getProfile());
//        safetyUser.setPhone(originUser.getPhone());
//        safetyUser.setEmail(originUser.getEmail());
        // 脱敏处理电话号码，替换敏感信息
        safetyUser.setPhone(originUser.getPhone() != null ?
                originUser.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null);
        // 脱敏处理电子邮件
        safetyUser.setEmail(originUser.getEmail() != null ?
                originUser.getEmail().replaceAll("(^\\w)[^@]*(@.*$)", "$1****$2") : null);
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 通过查询标签，得到用户
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> getUserByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //进行性能优化是不断调试的，以下用内存来查询
       QueryWrapper queryWrapper=new QueryWrapper<>();
        //1.先查询所有用户
           List<User> userList=userMapper.selectList(queryWrapper);
        //startTime =System.currentTimeMillis();
        //序列化
        Gson gson =new Gson();
        //2.在内存中判断是否包含要求的标签，内存查询很灵活
        return userList.stream().filter(user->{
            //如果用并行流parallelStream有缺点，面试会问。这个并行流有一个默认的公共线程池，如果有利用数据库查询的量数据的也用这个线程池查询
            //那么线程池的线程库可能就都被用光了，当前操作就无法执行
            //for(User user:userList){ stream().filter()语法糖来代替foreach遍历
                String tagsStr=user.getTags();
                //校验，从数据库取出来可能为空
                if(StringUtils.isBlank(tagsStr)){
                    return false;
                }
                //拿到的tags是json字符串，我们要使用就要转化为对象(反序列化)
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
                //如果tempTagNameSet为空，用orElse赋值，利用Optional消除if分支
             tempTagNameSet=Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>()) ;
            //使用集合要记得判空，用集合我们就可以用O（1）的时间复杂度立即判断集合是否包含某个标签
                for(String tagName:tagNameList){
                    if(!tempTagNameSet.contains(tagName)){
                        //返回false被过滤掉
                        return false;
                    }
                }
                return true;
            //}
        }).map(this::getSafetyUser).collect(Collectors.toList());
        //return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
      // log.info("memory query time="+(System.currentTimeMillis()-startTime));
        //return userList;
    }

    @Override
    public List<User> getUserByTagsSql(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper();
        //拼接and查询
        //like '%aa%' and like '%bb%'
        for(String tagName:tagNameList){
            queryWrapper=queryWrapper.like("tags",tagName);
        }
       List<User> userList= userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




