package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.DiningPoint;
import com.sky.entity.Dish;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.service.DiningPointService;
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
    private DiningPointService diningPointService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId, Long diningPointId) {
        log.info("根据分类和助餐点查询菜品: categoryId={}, diningPointId={}", categoryId, diningPointId);

        validateOrderableDiningPoint(diningPointId);
        String key = "dish_" + diningPointId + "_" + (categoryId == null ? "all" : categoryId);
        String json = (String) redisTemplate.opsForValue().get(key);
        List<DishVO> cachedList = json == null ? null : JSON.parseArray(json, DishVO.class);
        if (cachedList != null) {
            if (isValidFamilyDishCache(cachedList, diningPointId)) {
                log.info("从缓存中获取菜品列表: categoryId={}, diningPointId={}", categoryId, diningPointId);
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

    private void validateOrderableDiningPoint(Long diningPointId) {
        if (diningPointId == null) {
            throw new BaseException(MessageConstant.USER_DISH_QUERY_DINING_POINT_REQUIRED);
        }

        DiningPoint diningPoint = diningPointService.getById(diningPointId);
        if (diningPoint == null) {
            throw new BaseException(MessageConstant.DINING_POINT_NOT_FOUND);
        }
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new BaseException(MessageConstant.DINING_POINT_RESTING);
        }
    }

    private boolean isValidFamilyDishCache(List<DishVO> dishList, Long diningPointId) {
        for (DishVO dish : dishList) {
            if (dish.getDiningPointId() == null || !diningPointId.equals(dish.getDiningPointId())) {
                return false;
            }
            if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
                return false;
            }
        }
        return true;
    }
}
