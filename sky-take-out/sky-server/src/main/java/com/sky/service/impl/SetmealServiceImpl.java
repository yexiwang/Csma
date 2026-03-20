package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.DiningPoint;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DiningPointMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DiningPointMapper diningPointMapper;

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        validateSetmealSupply(setmealDTO);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        setmealMapper.insert(setmeal);

        bindSetmealId(setmealDTO.getSetmealDishes(), setmeal.getId());
        setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal != null && StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(setmealId -> {
            setmealMapper.deleteById(setmealId);
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        SetmealVO setmealVO = setmealMapper.getByIdWithDiningPoint(id);
        if (setmealVO == null) {
            return null;
        }
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        validateSetmealSupply(setmealDTO);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        bindSetmealId(setmealDTO.getSetmealDishes(), setmealId);
        setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal currentSetmeal = setmealMapper.getById(id);
        if (currentSetmeal == null) {
            throw new BaseException("套餐不存在");
        }

        if (StatusConstant.ENABLE.equals(status)) {
            if (currentSetmeal.getDiningPointId() == null) {
                throw new BaseException(MessageConstant.SETMEAL_DINING_POINT_REQUIRED);
            }

            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if (CollectionUtils.isEmpty(dishList)) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_DISH_REQUIRED);
            }

            for (Dish dish : dishList) {
                if (StatusConstant.DISABLE.equals(dish.getStatus())) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
                if (dish.getDiningPointId() == null) {
                    throw new BaseException(MessageConstant.SETMEAL_DISH_DINING_POINT_REQUIRED);
                }
                if (!currentSetmeal.getDiningPointId().equals(dish.getDiningPointId())) {
                    throw new BaseException(MessageConstant.SETMEAL_DISH_DINING_POINT_MISMATCH);
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateId(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null || !StatusConstant.ENABLE.equals(setmeal.getStatus()) || setmeal.getDiningPointId() == null) {
            throw new BaseException(MessageConstant.SETMEAL_DINING_POINT_UNAVAILABLE);
        }

        DiningPoint diningPoint = diningPointMapper.getById(setmeal.getDiningPointId());
        if (diningPoint == null || !StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new BaseException(MessageConstant.SETMEAL_DINING_POINT_UNAVAILABLE);
        }
        return setmealMapper.getDishItemBySetmealId(id);
    }

    private void validateSetmealSupply(SetmealDTO setmealDTO) {
        if (setmealDTO.getDiningPointId() == null) {
            throw new BaseException(MessageConstant.SETMEAL_DINING_POINT_REQUIRED);
        }

        DiningPoint diningPoint = diningPointMapper.getById(setmealDTO.getDiningPointId());
        if (diningPoint == null) {
            throw new BaseException(MessageConstant.DINING_POINT_NOT_FOUND);
        }

        if (CollectionUtils.isEmpty(setmealDTO.getSetmealDishes())) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_REQUIRED);
        }

        if (setmealDTO.getSetmealDishes().stream().anyMatch(setmealDish -> setmealDish.getDishId() == null)) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_FOUND);
        }

        List<Long> dishIds = setmealDTO.getSetmealDishes().stream()
                .map(SetmealDish::getDishId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (dishIds.isEmpty()) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_FOUND);
        }

        List<Dish> dishList = dishMapper.getByIds(dishIds);
        if (dishList.size() != dishIds.size()) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_FOUND);
        }

        Map<Long, Dish> dishMap = dishList.stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
            Dish dish = dishMap.get(setmealDish.getDishId());
            if (dish == null) {
                throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_FOUND);
            }
            if (dish.getDiningPointId() == null) {
                throw new BaseException(MessageConstant.SETMEAL_DISH_DINING_POINT_REQUIRED);
            }
            if (!setmealDTO.getDiningPointId().equals(dish.getDiningPointId())) {
                throw new BaseException(MessageConstant.SETMEAL_DISH_DINING_POINT_MISMATCH);
            }
        }
    }

    private void bindSetmealId(List<SetmealDish> setmealDishes, Long setmealId) {
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
    }
}
