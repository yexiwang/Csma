package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.DiningPoint;
import com.sky.entity.Dish;
import com.sky.entity.Elderly;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DiningPointMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ElderlyMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.service.support.FamilyCheckoutCalculator;
import com.sky.vo.ShoppingCartSummaryVO;
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
    @Autowired
    private DiningPointMapper diningPointMapper;
    @Autowired
    private FamilyCheckoutCalculator familyCheckoutCalculator;

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
        DiningPoint diningPoint = requireOrderableDiningPoint(elderly.getDiningPointId());
        Dish dish = requireOrderableDish(dishId);
        validateDishDiningPoint(dish, diningPoint.getId());
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
        List<ShoppingCart> cartList = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (CollectionUtils.isEmpty(cartList)) {
            return Collections.emptyList();
        }

        List<ShoppingCart> validCartList = new ArrayList<>();
        for (ShoppingCart cartItem : cartList) {
            Dish dish = getValidFamilyCartDish(cartItem);
            if (dish != null) {
                cartItem.setDiningPointId(dish.getDiningPointId());
                validCartList.add(cartItem);
                continue;
            }

            log.warn("remove invalid shopping cart item, id={}, userId={}, dishId={}, setmealId={}",
                    cartItem.getId(), userId, cartItem.getDishId(), cartItem.getSetmealId());
            shoppingCartMapper.deleteById(cartItem.getId());
        }
        return validCartList;
    }

    @Override
    public ShoppingCartSummaryVO getShoppingCartSummary(Long elderId, Integer tablewareStatus, Integer tablewareNumber) {
        requireFamilyRole();
        Long userId = BaseContext.getCurrentId();
        Long targetElderId = requireTargetElderId(elderId);
        Elderly elderly = requireAccessibleElderly(targetElderId, userId);
        DiningPoint diningPoint = requireDiningPoint(elderly.getDiningPointId());

        List<ShoppingCart> cartList = showShoppingCart();
        validateCartBinding(cartList, targetElderId);
        validateCartDiningPoint(cartList, diningPoint.getId());

        return familyCheckoutCalculator.calculate(
                targetElderId,
                diningPoint.getId(),
                cartList,
                tablewareStatus,
                tablewareNumber
        );
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

    @Override
    public void removeShoppingCart(ShoppingCartDTO shoppingCartDTO) {
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

        shoppingCartMapper.deleteById(cartList.get(0).getId());
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

    private DiningPoint requireDiningPoint(Long diningPointId) {
        DiningPoint diningPoint = diningPointMapper.getById(diningPointId);
        if (diningPoint == null) {
            throw new ShoppingCartBusinessException(MessageConstant.DINING_POINT_NOT_FOUND);
        }
        return diningPoint;
    }

    private DiningPoint requireOrderableDiningPoint(Long diningPointId) {
        DiningPoint diningPoint = requireDiningPoint(diningPointId);
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new ShoppingCartBusinessException(MessageConstant.DINING_POINT_RESTING);
        }
        return diningPoint;
    }

    private void ensureCartBinding(Long userId, Long elderId) {
        List<ShoppingCart> cartList = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        validateCartBinding(cartList, elderId);
    }

    private void validateCartBinding(List<ShoppingCart> cartList, Long elderId) {
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

    private void validateCartDiningPoint(List<ShoppingCart> cartList, Long expectedDiningPointId) {
        for (ShoppingCart cartItem : cartList) {
            if (cartItem.getSetmealId() != null) {
                throw new ShoppingCartBusinessException("当前版本暂不支持套餐点餐");
            }
            if (cartItem.getDishId() == null) {
                throw new ShoppingCartBusinessException("购物车菜品数据异常，请刷新后重试");
            }

            Long diningPointId = cartItem.getDiningPointId();
            if (diningPointId == null) {
                Dish dish = requireOrderableDish(cartItem.getDishId());
                diningPointId = dish.getDiningPointId();
            }
            if (!expectedDiningPointId.equals(diningPointId)) {
                throw new ShoppingCartBusinessException("购物车菜品不属于当前老人对应助餐点");
            }
        }
    }

    private void validateDishDiningPoint(Dish dish, Long expectedDiningPointId) {
        if (!expectedDiningPointId.equals(dish.getDiningPointId())) {
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

        DiningPoint diningPoint = diningPointMapper.getById(dish.getDiningPointId());
        if (diningPoint == null || !StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new ShoppingCartBusinessException(MessageConstant.DISH_DINING_POINT_UNAVAILABLE);
        }
        return dish;
    }

    private Dish getValidFamilyCartDish(ShoppingCart cartItem) {
        if (cartItem.getId() == null || cartItem.getDishId() == null || cartItem.getSetmealId() != null) {
            return null;
        }

        Dish dish = dishMapper.getById(cartItem.getDishId());
        if (dish == null || !StatusConstant.ENABLE.equals(dish.getStatus()) || dish.getDiningPointId() == null) {
            return null;
        }
        return dish;
    }
}
