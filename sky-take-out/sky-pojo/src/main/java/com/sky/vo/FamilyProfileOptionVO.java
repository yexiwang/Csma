package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "可选家属档案")
public class FamilyProfileOptionVO implements Serializable {

    @ApiModelProperty("家属档案ID")
    private Long id;

    @ApiModelProperty("关联的 FAMILY 用户ID")
    private Long userId;

    @ApiModelProperty("FAMILY 用户账号")
    private String username;

    @ApiModelProperty("家属姓名")
    private String name;

    @ApiModelProperty("联系电话")
    private String phone;
}
