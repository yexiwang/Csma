package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "志愿者个人概览数据")
public class VolunteerOverviewVO implements Serializable {

    @ApiModelProperty("志愿者姓名")
    private String name;

    @ApiModelProperty("状态 0禁用 1启用")
    private Integer status;

    @ApiModelProperty("累计服务单量")
    private Integer totalOrders;

    @ApiModelProperty("累计服务时长（小时）")
    private BigDecimal totalHours;

    @ApiModelProperty("综合评分")
    private BigDecimal rating;

    @ApiModelProperty("等级")
    private Integer level;
}
