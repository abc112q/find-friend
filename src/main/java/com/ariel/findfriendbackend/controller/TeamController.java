package com.ariel.findfriendbackend.controller;

import com.ariel.findfriendbackend.common.BaseResponse;
import com.ariel.findfriendbackend.common.ErrorCode;
import com.ariel.findfriendbackend.common.ResultUtils;
import com.ariel.findfriendbackend.dto.TeamQuery;
import com.ariel.findfriendbackend.exception.BusinessException;
import com.ariel.findfriendbackend.model.domain.Team;
import com.ariel.findfriendbackend.model.domain.User;
import com.ariel.findfriendbackend.model.enums.TeamStatusEnum;
import com.ariel.findfriendbackend.model.request.TeamAddRequest;
import com.ariel.findfriendbackend.service.TeamService;
import com.ariel.findfriendbackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Ariel
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000/"})
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    /**
     * 全局异常处理和拦截器进行权限校验的区别
     * 全局异常处理：已经走进方法了，才发现这个人没有权限然后报错
     * 拦截器：还没具体到执行逻辑就发现他没权限，然后拦截
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(Long id){
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result= teamService.removeById(id);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result= teamService.updateById(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询单个数据
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team= teamService.getById(id);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"获取队伍信息失败");
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询所有数据
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        //把一个对象的字段全部复制到另一个对象，避免使用大量set/get方法,因为返回的方法只接受Team泛型
        BeanUtils.copyProperties(teamQuery,team);
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        List<Team> teamList =teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        //把一个对象的字段全部复制到另一个对象，避免使用大量set/get方法,因为返回的方法只接受Team泛型
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page=new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> teamPage =teamService.page(page,queryWrapper);
        return ResultUtils.success(teamPage);
    }
}
