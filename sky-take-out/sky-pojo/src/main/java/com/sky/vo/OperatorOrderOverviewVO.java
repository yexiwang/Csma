package com.sky.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperatorOrderOverviewVO implements Serializable {

    private Integer pendingPrepare;

    private Integer preparing;

    private Integer pendingAssignment;

    private Integer mealReady;

    private Integer delivering;

    private Integer completed;
}
