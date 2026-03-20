package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 工作台
 */
@RestController
@RequestMapping("/admin/workspace")
//@Slf4j
@Api(tags = "工作台相关接口")
public class WorkSpaceController {

    private static final Logger log = LoggerFactory.getLogger(WorkSpaceController.class);

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 工作台今日数据查询
     * @return  turnover;//营业额 validOrderCount;//有效订单数  orderCompletionRate;//订单完成率 unitPrice;//平均客单价 newUsers;//新增用户数
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台今日数据查询")
    public Result<BusinessDataVO> businessData(){
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        log.info("工作台今日数据查询区间为{}到{}", begin, end);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return 待接单、待派送、已完成、已取消、总订单数
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("工作台查询订单管理数据")
    public Result<OrderOverViewVO> orderOverView(){
        log.info("工作台查询订单管理数据");
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * 查询菜品总览
     * @return 已启售数量 sold; 已停售数量 discontinued
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("工作台查询菜品总览")
    public Result<DishOverViewVO> dishOverView(){
        log.info("工作台查询菜品总览");
        return Result.success(workspaceService.getDishOverView());
    }

    /**
     * 查询套餐总览
     * @return 已启售数量 sold; 已停售数量 discontinued
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("工作台查询套餐总览")
    public Result<SetmealOverViewVO> setmealOverView(){
        log.info("工作台查询套餐总览");
        return Result.success(workspaceService.getSetmealOverView());
    }
}
