package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.entity.DiningPoint;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DiningPointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/diningPoint")
@Api(tags = "助餐点管理接口")
public class DiningPointController {

    private static final Logger log = LoggerFactory.getLogger(DiningPointController.class);

    @Autowired
    private DiningPointService diningPointService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增助餐点")
    public Result save(@RequestBody DiningPoint diningPoint) {
        log.info("新增助餐点: {}", diningPoint);
        diningPointService.save(diningPoint);
        clearUserSaleCache();
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("助餐点分页查询")
    public Result<PageResult> page(int page, int pageSize, String name, Integer status) {
        log.info("助餐点分页查询: page={}, pageSize={}, name={}, status={}", page, pageSize, name, status);
        Page<DiningPoint> pageResult = diningPointService.pageQuery(page, pageSize, name, status);
        return Result.success(new PageResult(pageResult.getTotal(), pageResult.getResult()));
    }

    @GetMapping("/list")
    @ApiOperation("查询助餐点下拉列表")
    public Result<List<DiningPoint>> list(@RequestParam(required = false) Integer status) {
        log.info("查询助餐点下拉列表: status={}", status);
        return Result.success(diningPointService.list(status));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询助餐点")
    public Result<DiningPoint> getById(@PathVariable Long id) {
        log.info("根据id查询助餐点: {}", id);
        return Result.success(diningPointService.getById(id));
    }

    @PutMapping
    @ApiOperation("修改助餐点")
    public Result update(@RequestBody DiningPoint diningPoint) {
        log.info("修改助餐点: {}", diningPoint);
        diningPointService.update(diningPoint);
        clearUserSaleCache();
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除助餐点")
    public Result delete(@PathVariable Long id) {
        log.info("删除助餐点: {}", id);
        diningPointService.deleteById(id);
        clearUserSaleCache();
        return Result.success();
    }

    private void clearUserSaleCache() {
        clearCache("dish_*");
        clearCache("setmealCache*");
    }

    private void clearCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
