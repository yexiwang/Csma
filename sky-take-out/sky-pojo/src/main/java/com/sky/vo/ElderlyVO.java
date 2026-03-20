package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "管理员老人档案返回数据")
public class ElderlyVO implements Serializable {

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

    @ApiModelProperty("所属助餐点名称")
    private String diningPointName;

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

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
