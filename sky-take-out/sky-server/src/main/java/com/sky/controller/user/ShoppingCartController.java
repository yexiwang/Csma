package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartSummaryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");
        return Result.success(shoppingCartService.showShoppingCart());
    }

    @GetMapping("/summary")
    @ApiOperation("查看购物车结算汇总")
    public Result<ShoppingCartSummaryVO> summary(@RequestParam Long elderId,
                                                 @RequestParam(required = false) Integer tablewareStatus,
                                                 @RequestParam(required = false) Integer tablewareNumber) {
        log.info("查看购物车结算汇总，elderId={}, tablewareStatus={}, tablewareNumber={}",
                elderId, tablewareStatus, tablewareNumber);
        return Result.success(shoppingCartService.getShoppingCartSummary(elderId, tablewareStatus, tablewareNumber));
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("减少购物车单个商品数量")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车商品数量，商品信息：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @PostMapping("/remove")
    @ApiOperation("删除购物车整项商品")
    public Result remove(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车整项商品，商品信息：{}", shoppingCartDTO);
        shoppingCartService.removeShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
