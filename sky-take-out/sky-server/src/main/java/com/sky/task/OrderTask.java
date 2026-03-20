package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务类  定时处理订单状态
 */
@Component//把普通pojo实例化到spring容器中，相当于配置文件中的<bean id="" class=""/>
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     *
     * 情况：下单后未支付，订单一直处于“1待支付”状态
     * 处理：通过定时任务每分钟检查一次是否存在支付超时订单（下单后超过15分钟仍未支付则判定为支付超时订单），如果存在则修改订单状态为“已取消”
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutTask(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        //select * from orders where status = 1 and order_time < (当前时间-15分钟)
        LocalDateTime  time =LocalDateTime.now().minusMinutes(15);//获取当前时间减去15分钟的时间
        List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                //更新订单状态为“已取消”
                orders.setStatus(Orders.CANCELLED);//订单状态 6取消
                orders.setCancelReason("支付超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直待派送订单
     *
     * 情况：用户收货后管理端未点击完成按钮，订单一直处于“4派送中”状态
     * 处理：通过定时任务每天凌晨1点检查一次是否存在“4派送中”的订单，如果存在则修改订单状态为“5已完成”
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理待派送中的订单：{}", LocalDateTime.now());

        //select * from orders where status = 4 and order_time < (当前时间-1天)
        LocalDateTime  time =LocalDateTime.now().minusDays(1);
        List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                //更新订单状态为“已取消”
                orders.setStatus(Orders.COMPLETED);//订单状态 5已完成
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }
}
