package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")//注解表示这个类是一个处理HTTP请求的控制器，并且会自动将方法返回值序列化为JSON格式直接写入HTTP响应体中，用于构建RESTful风格的Web服务。
@RequestMapping("/user/shop")//访问路径
//@Slf4j//日志记录器，用于记录日志信息
@Api(tags = "店铺操作相关接口")//Swagger注解，用于给API接口分类
public class ShopController {

    private static final Logger log = LoggerFactory.getLogger(ShopController.class);

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取店铺营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")  //没有从前端传来的参数，纯粹取数据，所以不需要参数
    public Result getStatus() {
        String statusStr = (String) redisTemplate.opsForValue().get(KEY);
        Integer status = statusStr != null ? Integer.valueOf(statusStr) : null;
        log.info("获取店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
