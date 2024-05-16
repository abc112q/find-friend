package com.ariel.findfriendbackend.service;


import com.ariel.findfriendbackend.dto.TeamQuery;
import com.ariel.findfriendbackend.model.domain.Team;
import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.model.request.TeamJoinRequest;
import com.ariel.findfriendbackend.model.request.TeamQuitRequest;
import com.ariel.findfriendbackend.model.request.TeamUpdateRequest;
import com.ariel.findfriendbackend.model.vo.TeamUserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Ariel
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-05-13 21:27:38
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍新消息
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param id,loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    boolean deleteTeam(long id, User loginUser);
}
