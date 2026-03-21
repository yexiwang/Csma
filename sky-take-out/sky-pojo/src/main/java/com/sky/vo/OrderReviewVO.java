package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "订单评价展示数据")
public class OrderReviewVO implements Serializable {

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("评分 1~5")
    private Integer score;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
