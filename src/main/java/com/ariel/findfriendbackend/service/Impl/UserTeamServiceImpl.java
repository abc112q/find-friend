package com.ariel.findfriendbackend.service.Impl;

import com.ariel.findfriendbackend.mapper.UserTeamMapper;
import com.ariel.findfriendbackend.model.domain.UserTeam;
import com.ariel.findfriendbackend.service.UserTeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author Ariel
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-05-13 21:35:48
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




