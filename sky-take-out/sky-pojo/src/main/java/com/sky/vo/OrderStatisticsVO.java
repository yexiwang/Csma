package com.sky.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Three-card order statistics used by the current admin dashboard.
 * Legacy field names are kept for compatibility.
 */
@Data
public class OrderStatisticsVO implements Serializable {

    /**
     * status 2 待调度
     */
    private Integer toBeConfirmed;

    /**
     * status 3 制作中
     */
    private Integer confirmed;

    /**
     * status 4 待取餐
     */
    private Integer deliveryInProgress;
}
