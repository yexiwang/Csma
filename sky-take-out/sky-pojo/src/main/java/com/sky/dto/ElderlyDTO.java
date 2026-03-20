package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "管理员新增或编辑老人档案时传递的数据模型")
public class ElderlyDTO implements Serializable {

    @ApiModelProperty("老人ID")
    private Long id;

    @ApiModelProperty("关联用户ID(家属或本人)")
    private Long userId;

    @ApiModelProperty("老人姓名")
    private String name;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("详细居住地址")
    private String address;

    @ApiModelProperty("所属助餐点ID/默认助餐点ID")
    private Long diningPointId;

    @ApiModelProperty("所属网格/片区")
    private String gridCode;

    @ApiModelProperty("健康状况")
    private String healthInfo;

    @ApiModelProperty("特殊需求")
    private String specialNeeds;

    @ApiModelProperty("身份证号")
    private String idCard;

    @ApiModelProperty("照片")
    private String image;
}
