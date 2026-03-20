package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Order overview for today's dashboard.
 * Legacy field names are kept for compatibility with the current frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO implements Serializable {

    /**
     * status 2 待调度
     */
    private Integer waitingOrders;

    /**
     * status 3 制作中
     */
    private Integer deliveredOrders;

    /**
     * status 6 已完成
     */
    private Integer completedOrders;

    /**
     * status 7 已取消
     */
    private Integer cancelledOrders;

    private Integer allOrders;
}
