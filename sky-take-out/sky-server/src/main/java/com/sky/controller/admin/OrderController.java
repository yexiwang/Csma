package com.sky.controller.admin;

import com.sky.dto.OperatorAssignVolunteerDTO;
import com.sky.dto.OperatorOrderBoardQueryDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.VolunteerService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OperatorOrderOverviewVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
//@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private VolunteerService volunteerService;

    /**
     * 订单搜索
     *
     *业务规则：
     *
     * 输入订单号/手机号进行搜索，支持模糊搜索
     * 根据订单状态进行筛选
     * 下单时间进行时间筛选
     * 搜索内容为空，提示未找到相关订单
     * 搜索结果页，展示包含搜索关键词的内容
     * 分页展示搜索到的订单数据
     * @param ordersPageQueryDTO  Query参数
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计  返回 2待接单数量-toBeConfirmed  3待派送数量-confirmed  4派送中数量-deliveryInProgress
     *
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("--各个状态的订单数量统计--");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 订单详情
     *
     *业务规则：
     *
     * 订单详情页面需要展示订单基本信息（状态、订单号、下单时间、收货人、电话、收货地址、金额等）
     * 订单详情页面需要展示订单明细数据（商品名称、数量、单价）
     * @param id Path参数 订单id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情：{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    @GetMapping("/operator/board")
    @ApiOperation("操作员查询本站点订单看板")
    public Result<PageResult> operatorBoard(OperatorOrderBoardQueryDTO queryDTO) {
        log.info("操作员订单看板查询：{}", queryDTO);
        return Result.success(orderService.operatorBoard(queryDTO));
    }

    @GetMapping("/operator/overview")
    @ApiOperation("操作员查询本站点执行概览")
    public Result<OperatorOrderOverviewVO> operatorOverview() {
        log.info("操作员查询本站点执行概览");
        return Result.success(orderService.operatorOverview());
    }

    @GetMapping("/operator/details/{id}")
    @ApiOperation("操作员查询本站点订单详情")
    public Result<OrderVO> operatorDetails(@PathVariable Long id) {
        log.info("操作员查询本站点订单详情：{}", id);
        return Result.success(orderService.details(id));
    }

    @GetMapping("/operator/volunteers")
    @ApiOperation("操作员查询可分配志愿者")
    public Result listOperatorVolunteers() {
        log.info("操作员查询可分配志愿者");
        return Result.success(volunteerService.listActiveVolunteers());
    }

    /**
     * 调度派单 (原接单逻辑)
     * 业务规则：
     * 管理员将订单指派给志愿者，订单状态修改为“已接单/制作中”
     * @param ordersConfirmDTO  Path参数 订单id
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("调度派单")
    public Result dispatch(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("调度派单，订单ID：{}，志愿者ID：{}", ordersConfirmDTO.getId(), ordersConfirmDTO.getVolunteerId());
        orderService.dispatch(ordersConfirmDTO.getId(), ordersConfirmDTO.getVolunteerId());
        return Result.success();
    }

    @PutMapping("/operator/startPreparing/{id}")
    @ApiOperation("操作员开始制作")
    public Result startPreparing(@PathVariable Long id) {
        log.info("操作员开始制作：{}", id);
        orderService.startPreparing(id);
        return Result.success();
    }

    @PutMapping("/operator/assignVolunteer")
    @ApiOperation("操作员分配志愿者")
    public Result assignVolunteer(@RequestBody OperatorAssignVolunteerDTO assignVolunteerDTO) {
        log.info("操作员分配志愿者：{}", assignVolunteerDTO);
        orderService.assignVolunteer(assignVolunteerDTO);
        return Result.success();
    }

    @PutMapping("/operator/mealReady/{id}")
    @ApiOperation("操作员标记出餐完成")
    public Result operatorMealReady(@PathVariable Long id) {
        log.info("操作员标记出餐完成：{}", id);
        orderService.markMealReady(id);
        return Result.success();
    }

    @PutMapping("/operator/pickup/{id}")
    @ApiOperation("操作员确认志愿者取餐")
    public Result operatorPickup(@PathVariable Long id) {
        log.info("操作员确认志愿者取餐：{}", id);
        orderService.confirmPickup(id);
        return Result.success();
    }

    /**
     * 兼容原后台页面的拒单接口
     *
     * @param ordersRejectionDTO 拒单信息
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒单：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 兼容原后台页面的取消订单接口
     *
     * @param ordersCancelDTO 取消订单信息
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }
    
    /**
     * 助餐点标记出餐
     * @param id 订单ID
     * @return
     */
    @PutMapping("/mealReady/{id}")
    @ApiOperation("助餐点标记出餐")
    public Result markMealReady(@PathVariable("id") Long id) {
        log.info("助餐点标记出餐：{}", id);
        orderService.markMealReady(id);
        return Result.success();
    }

    /**
     * 志愿者确认取餐 (原派送订单)
     * @param id Path参数 订单id
     *           业务规则：
     *
     * 派送订单其实就是将订单状态修改为“4派送中”
     * 只有状态为“待取餐”的订单可以执行派送订单操作
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("志愿者确认取餐")
    public Result delivery(@PathVariable("id") Long id) {
        log.info("志愿者确认取餐：{}", id);
        orderService.confirmPickup(id);
        return Result.success();
    }

    /**
     * 志愿者确认送达 (原完成订单)
     * @param id Path参数 订单id
     * 业务规则：
     *
     * 完成订单其实就是将订单状态修改为“已完成”
     * 只有状态为“派送中”的订单可以执行订单完成操作
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("志愿者确认送达")
    public Result complete(@PathVariable("id") Long id) {
        log.info("志愿者确认送达：{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
