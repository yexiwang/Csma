package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Order scheduler for the community meal-delivery workflow.
 * It only handles safe timeout cleanup and overdue warning logs.
 */
@Component
@Slf4j
public class OrderTask {

    private static final long PAYMENT_TIMEOUT_MINUTES = 15;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * Cancel unpaid orders that have timed out.
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutTask() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutBefore = now.minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        log.info("process payment timeout orders, now={}, timeoutBefore={}", now, timeoutBefore);

        for (Orders order : orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, timeoutBefore)) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("支付超时自动取消");
            order.setCancelTime(now);
            orderMapper.update(order);
        }
    }

    /**
     * Log overdue in-flight orders for manual follow-up. The task does not mutate order status.
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void processAbnormalOrderWarningTask() {
        LocalDateTime now = LocalDateTime.now();
        logOverdueOrders(Orders.CONFIRMED, "preparing_overtime", now);
        logOverdueOrders(Orders.MEAL_READY, "meal_ready_overtime", now);
        logOverdueOrders(Orders.DELIVERY_IN_PROGRESS, "delivering_overtime", now);
    }

    private void logOverdueOrders(Integer status, String label, LocalDateTime now) {
        Integer count = orderMapper.countOverdueByStatus(status, now);
        int overdueCount = count == null ? 0 : count;
        if (overdueCount > 0) {
            log.warn("detected overdue orders, label={}, status={}, count={}, now={}", label, status, overdueCount, now);
        }
    }
}
