package com.ariel.findfriendbackend.service;


import com.ariel.findfriendbackend.model.domain.Team;
import com.ariel.findfriendbackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Ariel
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-05-13 21:27:38
*/
public interface TeamService extends IService<Team> {

     long addTeam(Team team, User loginUser);
}
