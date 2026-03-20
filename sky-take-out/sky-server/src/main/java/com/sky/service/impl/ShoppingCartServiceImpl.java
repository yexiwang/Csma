package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Elderly;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ElderlyMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final String ROLE_FAMILY = "FAMILY";

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        requireFamilyRole();
        Long userId = BaseContext.getCurrentId();
        Long elderId = requireTargetElderId(shoppingCartDTO.getElderId());
        Long dishId = shoppingCartDTO.getDishId();
        if (dishId == null) {
            throw new ShoppingCartBusinessException("当前版本暂不支持套餐点餐");
        }

        Elderly elderly = requireAccessibleElderly(elderId, userId);
        Dish dish = requireOrderableDish(dishId);
        validateDishDiningPoint(dish, elderly);
        ensureCartBinding(userId, elderId);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> existingCartList = shoppingCartMapper.list(shoppingCart);
        if (!CollectionUtils.isEmpty(existingCartList)) {
            ShoppingCart existingCart = existingCartList.get(0);
            existingCart.setNumber(existingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existingCart);
            return;
        }

        shoppingCart.setName(dish.getName());
        shoppingCart.setImage(dish.getImage());
        shoppingCart.setAmount(dish.getPrice());
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        if (CollectionUtils.isEmpty(cartList)) {
            return Collections.emptyList();
        }

        List<ShoppingCart> validCartList = new ArrayList<>();
        for (ShoppingCart cartItem : cartList) {
            Dish dish = getValidFamilyCartDish(cartItem);
            if (dish != null) {
                cartItem.setDiningPointId(dish.getDiningPointId());
                validCartList.add(cartItem);
            } else {
                log.warn("remove invalid shopping cart item, id={}, userId={}, dishId={}, setmealId={}",
                        cartItem.getId(), userId, cartItem.getDishId(), cartItem.getSetmealId());
                shoppingCartMapper.deleteById(cartItem.getId());
            }
        }
        return validCartList;
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        requireFamilyRole();
        Long userId = BaseContext.getCurrentId();
        Long elderId = requireTargetElderId(shoppingCartDTO.getElderId());
        requireAccessibleElderly(elderId, userId);
        ensureCartBinding(userId, elderId);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        if (CollectionUtils.isEmpty(cartList)) {
            return;
        }

        ShoppingCart currentCart = cartList.get(0);
        Integer number = currentCart.getNumber();
        if (number == null || number <= 1) {
            shoppingCartMapper.deleteById(currentCart.getId());
            return;
        }

        currentCart.setNumber(number - 1);
        shoppingCartMapper.updateNumberById(currentCart);
    }

    private void requireFamilyRole() {
        String role = BaseContext.getCurrentRole();
        if (!ROLE_FAMILY.equals(role)) {
            throw new ShoppingCartBusinessException("当前账号无法操作家属购物车");
        }
    }

    private Long requireTargetElderId(Long elderId) {
        if (elderId == null) {
            throw new ShoppingCartBusinessException("请先选择服务老人");
        }
        return elderId;
    }

    private Elderly requireAccessibleElderly(Long elderId, Long userId) {
        Elderly elderly = elderlyMapper.getById(elderId);
        if (elderly == null) {
            throw new ShoppingCartBusinessException("服务老人不存在");
        }
        if (!userId.equals(elderly.getUserId())) {
            throw new ShoppingCartBusinessException("无权为该老人操作购物车");
        }
        if (elderly.getDiningPointId() == null) {
            throw new ShoppingCartBusinessException("当前老人未绑定助餐点，无法操作购物车");
        }
        return elderly;
    }

    private void ensureCartBinding(Long userId, Long elderId) {
        ShoppingCart query = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> cartList = shoppingCartMapper.list(query);
        if (CollectionUtils.isEmpty(cartList)) {
            return;
        }

        for (ShoppingCart cartItem : cartList) {
            if (cartItem.getElderId() == null) {
                throw new ShoppingCartBusinessException("购物车存在历史数据，请先清空购物车后重新选择老人");
            }
            if (!elderId.equals(cartItem.getElderId())) {
                throw new ShoppingCartBusinessException("购物车已绑定其他老人，请先清空购物车后再切换");
            }
        }
    }

    private void validateDishDiningPoint(Dish dish, Elderly elderly) {
        if (!elderly.getDiningPointId().equals(dish.getDiningPointId())) {
            throw new ShoppingCartBusinessException("所选菜品不属于当前老人对应助餐点");
        }
    }

    private Dish requireOrderableDish(Long dishId) {
        Dish dish = dishMapper.getById(dishId);
        if (dish == null) {
            throw new ShoppingCartBusinessException("菜品不存在，无法加入购物车");
        }
        if (!StatusConstant.ENABLE.equals(dish.getStatus())) {
            throw new ShoppingCartBusinessException("菜品已停售，无法加入购物车");
        }
        if (dish.getDiningPointId() == null) {
            throw new ShoppingCartBusinessException("菜品未绑定助餐点，无法加入购物车");
        }
        return dish;
    }

    private Dish getValidFamilyCartDish(ShoppingCart cartItem) {
        if (cartItem.getId() == null || cartItem.getDishId() == null || cartItem.getSetmealId() != null) {
            return null;
        }

        Dish dish = dishMapper.getById(cartItem.getDishId());
        if (dish == null
                || !StatusConstant.ENABLE.equals(dish.getStatus())
                || dish.getDiningPointId() == null) {
            return null;
        }
        return dish;
    }
}