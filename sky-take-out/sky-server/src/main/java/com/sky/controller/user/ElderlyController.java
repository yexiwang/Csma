package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.DiningPoint;
import com.sky.entity.Elderly;
import com.sky.mapper.DiningPointMapper;
import com.sky.result.Result;
import com.sky.service.ElderlyService;
import com.sky.vo.UserElderlyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userElderlyController")
@RequestMapping("/user/elderly")
@Slf4j
@Api(tags = "用户端老人接口")
public class ElderlyController {

    @Autowired
    private ElderlyService elderlyService;
    @Autowired
    private DiningPointMapper diningPointMapper;

    @GetMapping("/list")
    @ApiOperation("查询当前家属可管理的老人")
    public Result<List<UserElderlyVO>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询当前家庭用户的老年名单: {}", userId);
        List<Elderly> elderlyList = elderlyService.getByUserId(userId);
        List<UserElderlyVO> result = elderlyList.stream()
                .map(this::buildUserElderlyVO)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    private UserElderlyVO buildUserElderlyVO(Elderly elderly) {
        UserElderlyVO elderlyVO = new UserElderlyVO();
        BeanUtils.copyProperties(elderly, elderlyVO);
        if (elderly.getDiningPointId() != null) {
            DiningPoint diningPoint = diningPointMapper.getById(elderly.getDiningPointId());
            elderlyVO.setDiningPointName(diningPoint == null ? null : diningPoint.getName());
            elderlyVO.setDiningPointStatus(diningPoint == null ? null : diningPoint.getStatus());
        }
        return elderlyVO;
    }
}
