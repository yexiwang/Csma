package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController//注解表示这个类是一个处理HTTP请求的控制器，并且会自动将方法返回值序列化为JSON格式直接写入HTTP响应体中，用于构建RESTful风格的Web服务。
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理接口")
//@Slf4j
public class DishController {

    private static final Logger log = LoggerFactory.getLogger(DishController.class);

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO Body参数
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        String key="dish_" + dishDTO.getCategoryId();
        clearCache(key);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO  Query参数
     * @return
     */
    @GetMapping("/page")// "/admin/dish/page?categoryId=101&name=宫保鸡丁&page=1&pageSize=10&status=1"
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 批量删除菜品
     * @param ids  Query参数
     * @return
     */
    @DeleteMapping//admin/dish?ids=1,2,3
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {//@RequestParam List<Long> ids 表示：从HTTP DELETE请求中提取名为 ids 的参数
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBath(ids);

        //将所有菜品缓存清理
        clearCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return dishVO
     * 返回的数据并没有分类名称，但是实际上回显里又有分类名称，可能是前端进行了操作
     */
    @GetMapping("/{id}")//admin/dish/101
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO  Body参数
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //将所有菜品缓存清理
        clearCache("dish_*");

        return Result.success();
    }


    /**
     * 菜品起售停售
     * @param status  Path参数 1为起售，0为停售
     * @param id  Query参数  菜品id
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        log.info("菜品起售停售：{}", status);
        dishService.startOrStop(status,id);

        //将所有菜品缓存清理,所有以dish_开头的key
        clearCache("dish_*");

        return Result.success();
    }



    /**
     * 根据分类id查询菜品--用于套餐管理
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> list=dishService.list(categoryId);
        return Result.success(list);
    }


    /**
     * 统一清理缓存数据
     * @param pattern
     */
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }


}
