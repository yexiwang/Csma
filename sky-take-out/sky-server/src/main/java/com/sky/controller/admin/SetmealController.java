package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//注解表示这个类是一个处理HTTP请求的控制器，并且会自动将方法返回值序列化为JSON格式直接写入HTTP响应体中，用于构建RESTful风格的Web服务。
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐  同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO   Body参数
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }


    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO  Query 参数
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids  Query 参数
     * @return
     *
     * @RequestParam List<Long> ids 表示：
     * 从HTTP DELETE请求中提取名为 ids 的参数
     * 将参数值转换为 List<Long> 类型
     * 绑定到方法参数 ids 上
     */
    @DeleteMapping// /admin/setmeal?ids=1,2,3
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }


    /**
     * 根据套餐id查询套餐和关联菜品，用于回显数据
     *
     * @param id  Path 参数
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据套餐id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("当前要查询的套餐id：{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO  Body 参数
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐起售停售
     * @param status Path 参数
     * @param id Query 参数  套餐id
     * @return
     */
    @PostMapping("/status/{status}")//http://localhost/api/setmeal/status/1?id=32
    @ApiOperation("套餐起售停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id) {
        log.info("套餐起售停售：{}", status);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
