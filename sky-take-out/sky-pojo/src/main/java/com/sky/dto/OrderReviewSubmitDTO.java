package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户提交订单评价时传递的数据模型")
public class OrderReviewSubmitDTO implements Serializable {

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("评分 1~5")
    private Integer score;

    @ApiModelProperty("评价内容")
    private String content;
}
