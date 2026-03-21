package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.VolunteerService;
import com.sky.vo.VolunteerOverviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController("userVolunteerController")
@RequestMapping("/user/volunteer")
@Slf4j
@Api(tags = "用户端志愿者任务接口")
public class VolunteerController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VolunteerService volunteerService;

    @GetMapping("/orders")
    @ApiOperation("分页查询当前志愿者任务")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        log.info("志愿者查询 指定命令，志愿者ID={}, role={}, page={}, pageSize={}, status={}",
                BaseContext.getCurrentId(), BaseContext.getCurrentRole(), page, pageSize, status);
        return Result.success(orderService.pageQuery4Volunteer(page, pageSize, status));
    }

    @GetMapping("/overview")
    @ApiOperation("查询当前志愿者个人概览")
    public Result<VolunteerOverviewVO> overview() {
        log.info("查询当前志愿者个人概览，志愿者ID={}, role={}",
                BaseContext.getCurrentId(), BaseContext.getCurrentRole());
        return Result.success(volunteerService.getCurrentOverview());
    }

    @GetMapping("/overview/export")
    @ApiOperation("导出当前志愿者个人概览")
    public void exportOverview(HttpServletResponse response) {
        log.info("导出当前志愿者个人概览，志愿者ID={}, role={}",
                BaseContext.getCurrentId(), BaseContext.getCurrentRole());
        volunteerService.exportCurrentOverview(response);
    }

    @PutMapping("/pickup/{id}")
    @ApiOperation("志愿者确认取餐")
    public Result pickup(@PathVariable Long id) {
        log.info("志愿者确认取货，志愿者身份={}, role={}, orderId={}",
                BaseContext.getCurrentId(), BaseContext.getCurrentRole(), id);
        orderService.volunteerConfirmPickup(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("志愿者确认送达")
    public Result complete(@PathVariable Long id) {
        log.info("志愿者确认送货，志愿者身份证={}, role={}, orderId={}",
                BaseContext.getCurrentId(), BaseContext.getCurrentRole(), id);
        orderService.volunteerComplete(id);
        return Result.success();
    }
}
