package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.entity.DiningPoint;
import com.sky.result.Result;
import com.sky.service.DiningPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "助餐点营业状态接口")
public class ShopController {

    @Autowired
    private DiningPointService diningPointService;

    @PutMapping("/{status}")
    @ApiOperation("设置助餐点营业状态")
    public Result setStatus(@PathVariable Integer status) {
        Long diningPointId = BaseContext.getCurrentDiningPointId();
        if (diningPointId == null) {
            log.warn("操作员未绑定助餐点，无法设置营业状态");
            return Result.error("当前账号未绑定助餐点");
        }
        log.info("设置助餐点营业状态：{}，diningPointId={}", status == 1 ? "营业中" : "打烊中", diningPointId);
        DiningPoint diningPoint = diningPointService.getById(diningPointId);
        if (diningPoint == null) {
            return Result.error("助餐点不存在");
        }
        diningPoint.setStatus(status);
        diningPointService.update(diningPoint);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取助餐点营业状态")
    public Result<Integer> getStatus() {
        Long diningPointId = BaseContext.getCurrentDiningPointId();
        if (diningPointId == null) {
            log.warn("操作员未绑定助餐点，无法获取营业状态");
            return Result.error("当前账号未绑定助餐点");
        }
        DiningPoint diningPoint = diningPointService.getById(diningPointId);
        if (diningPoint == null) {
            return Result.error("助餐点不存在");
        }
        Integer status = diningPoint.getStatus();
        log.info("获取助餐点营业状态：{}，diningPointId={}", status == 1 ? "营业中" : "打烊中", diningPointId);
        return Result.success(status);
    }
}
