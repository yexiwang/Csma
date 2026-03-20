package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId, Long diningPointId) {
        log.info("根据分类id和助餐点查询菜品，categoryId={}, diningPointId={}", categoryId, diningPointId);

        String key = "dish_" + (diningPointId == null ? "all" : diningPointId) + "_" + (categoryId == null ? "all" : categoryId);
        String json = (String) redisTemplate.opsForValue().get(key);
        List<DishVO> cachedList = json == null ? null : JSON.parseArray(json, DishVO.class);
        if (cachedList != null && !cachedList.isEmpty()) {
            if (isValidFamilyDishCache(cachedList)) {
                log.info("从缓存中获取菜品列表，categoryId={}, diningPointId={}", categoryId, diningPointId);
                return Result.success(cachedList);
            }
            redisTemplate.delete(key);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setDiningPointId(diningPointId);
        dish.setStatus(StatusConstant.ENABLE);

        List<DishVO> dishList = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, JSON.toJSONString(dishList));
        return Result.success(dishList);
    }

    private boolean isValidFamilyDishCache(List<DishVO> dishList) {
        for (DishVO dish : dishList) {
            if (dish.getDiningPointId() == null || !StatusConstant.ENABLE.equals(dish.getStatus())) {
                return false;
            }
        }
        return true;
    }
}
