package com.sky.controller.user;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.DiningPoint;
import com.sky.entity.Setmeal;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.service.DiningPointService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端套餐浏览接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DiningPointService diningPointService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "'dp:' + #diningPointId + ':cat:' + (#categoryId == null ? 'all' : #categoryId)")
    public Result<List<Setmeal>> list(Long categoryId, Long diningPointId) {
        log.info("根据分类和助餐点查询套餐: categoryId={}, diningPointId={}", categoryId, diningPointId);
        validateOrderableDiningPoint(diningPointId);

        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setDiningPointId(diningPointId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        log.info("根据套餐id查询包含的菜品列表: {}", id);
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }

    private void validateOrderableDiningPoint(Long diningPointId) {
        if (diningPointId == null) {
            throw new BaseException(MessageConstant.USER_SETMEAL_QUERY_DINING_POINT_REQUIRED);
        }

        DiningPoint diningPoint = diningPointService.getById(diningPointId);
        if (diningPoint == null) {
            throw new BaseException(MessageConstant.DINING_POINT_NOT_FOUND);
        }
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new BaseException(MessageConstant.DINING_POINT_RESTING);
        }
    }
}
