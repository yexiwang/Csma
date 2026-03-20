package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.vo.ShoppingCartSummaryVO;

import java.util.List;

public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void cleanShoppingCart();

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    void removeShoppingCart(ShoppingCartDTO shoppingCartDTO);

    ShoppingCartSummaryVO getShoppingCartSummary(Long elderId, Integer tablewareStatus, Integer tablewareNumber);
}
