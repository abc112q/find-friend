package com.ariel.findfriendbackend.service.Impl;

import com.ariel.findfriendbackend.common.ErrorCode;
import com.ariel.findfriendbackend.dto.TeamQuery;
import com.ariel.findfriendbackend.exception.BusinessException;
import com.ariel.findfriendbackend.mapper.TeamMapper;
import com.ariel.findfriendbackend.model.domain.Team;
import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.model.domain.UserTeam;
import com.ariel.findfriendbackend.model.enums.TeamStatusEnum;
import com.ariel.findfriendbackend.model.request.TeamJoinRequest;
import com.ariel.findfriendbackend.model.request.TeamQuitRequest;
import com.ariel.findfriendbackend.model.request.TeamUpdateRequest;
import com.ariel.findfriendbackend.model.vo.TeamUserVo;
import com.ariel.findfriendbackend.model.vo.UserVo;
import com.ariel.findfriendbackend.service.TeamService;
import com.ariel.findfriendbackend.service.UserService;
import com.ariel.findfriendbackend.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Synchronized;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Ariel
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-05-13 21:27:38
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Synchronized
    /**
     * 创建队伍
     */
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 队伍查询展示
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVO = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVo userVO = new UserVo();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 只有登录用户才能使用
     * @param teamUpdateRequest
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id=teamUpdateRequest.getId();
        if(id==null || id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam =this.getById(id);
        if(oldTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //只有登录的用户或者管理员才能更新队伍信息
        if(!oldTeam.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //如果状态为私密，那么就要加上密码
        TeamStatusEnum statusEnum=TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }
        Team updateTeam =new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        boolean result=this.updateById(updateTeam);
        return result;
    }

    /**
     * 加入队伍，即在队伍用户关系表中新增一条
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {
        if(teamJoinRequest ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //用户最多只能加入5个队伍 todo 如果需要加入更多队伍请开通会员

        Long teamId=teamJoinRequest.getTeamId();
        Team team=getTeamById(teamId);
        if(team.getExpireTime()!=null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已经过期");
        }
        Integer status=team.getStatus();
        TeamStatusEnum statusEnum=TeamStatusEnum.getEnumByValue(status);
        //equals方法最好把确保不是空的值放在外
        if(TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"禁止加入私人队伍");
        }
        String password= teamJoinRequest.getPassword();
        if(StringUtils.isBlank(password) && !password.equals(team.getPassword())){
            throw new BusinessException(ErrorCode.NO_AUTH,"队伍密码错误");
        }
        //当前用户加入的队伍数量
        Long userId=loginUser.getId();
        QueryWrapper userTeamQueryWrapper=new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long hasJoinNum=userTeamService.count(userTeamQueryWrapper);
        if(hasJoinNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多加入5个队伍");
        }
        //不能重复加入同一个队伍
        userTeamQueryWrapper =new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        userTeamQueryWrapper.eq("teamId",teamId);
        long isUserJoined=userTeamService.count(userTeamQueryWrapper);
        if(isUserJoined>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能重复加入同一个队伍");
        }
        //已加入队伍的用户数量,在用户队伍关系表查队伍的数量
        long teamHasUser = this.getHasUser(teamId);
        if(teamHasUser>team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人满");
        }
        //修改队伍信息
        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }

    /**
     * 队伍有多少用户
     * @param teamId
     * @return
     */
    private long getHasUser(Long teamId) {
        QueryWrapper userTeamQueryWrapper =new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long teamHasUser=userTeamService.count(userTeamQueryWrapper);
        return teamHasUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验队伍是否存在，严谨点先校验id是否存在，再校验队伍是否存在
        Long teamId=teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //校验当前用户是否已经加入队伍
        Long userId = loginUser.getId();
        UserTeam queryUserTeam =new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>(queryUserTeam);
        Long count=userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没加入该队伍");
        }
        //队伍只有1人，解散
        if(getHasUser(teamId)==1){
            //删除队伍和所有加入队伍的关系，删除关系也是 复用 335的代码然后直接在最后返回的时候删除关系
            this.removeById(teamId);
        }else {
            //如果队伍至少有2人，判断要退出队伍的是不是队长
            if (team.getUserId().equals(userId)) {
                //如果队长要退出队伍，把队长转移给最早加入的2个用户
                //1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam>userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeadId= nextUserTeam.getUserId();
                //2.更新当前队伍队长
                Team updateTeam=new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeadId);
                boolean result= this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更换队长失败");
                }
            }
        }
        //移除关系 335行
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 获取队伍，并校验队伍是否存在
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if(teamId ==null|| teamId <=0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"");
        }
        Team team=this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }
}




