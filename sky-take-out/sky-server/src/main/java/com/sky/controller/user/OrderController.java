package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO  Body参数
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单参数为{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO =orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO  Body参数 orderNumber订单号   payMethod支付方式
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     *
     * @param page  页面
     * @param pageSize  每页数据条数
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * 业务规则
     *
     * 分页查询历史订单
     * 可以根据订单状态查询
     * @return 展示订单数据时，需要展示的数据包括：下单时间、订单状态、订单金额、订单明细（商品名称、图片）
     */
    @GetMapping("/historyOrders")//"/user/order/historyOrders?page=1&pageSize=10&status"
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        log.info("历史订单查询，页面：{}, 每页数据条数：{}, 订单状态：{}", page, pageSize, status);
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     *
     * @param id  Path参数 订单id
     * @return
     */
    @GetMapping("/orderDetail/{id}")//http://localhost:8080/user/order/orderDetail/9
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情，订单id为:{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 用户取消订单  Path参数 订单id
     *
     *业务规则：
     * 待支付和待接单状态下，用户可直接取消订单
     * 商家已接单状态下，用户取消订单需电话沟通商家
     * 派送中状态下，用户取消订单需电话沟通商家
     * 如果在待接单状态下取消订单，需要给用户退款
     * 取消订单后需要将订单状态修改为“已取消”
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id) throws Exception {
        log.info("用户取消订单，订单id为：{}", id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     *
     * @param id Path参数 订单id
     * 业务规则：
     * 再来一单就是将原订单中的商品重新加入到购物车中
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单，订单id为：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 催单
     *
     * @param id  Path参数 订单id
     * 约定服务端发送给客户端浏览器的数据格式为JSON，字段包括：type，orderId，content
     * - type 为消息类型，1为来单提醒 2为客户催单
     * - orderId 为订单id
     * - content 为消息内容
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable("id") Long id) {
        log.info("催单，订单id为：{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
