package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "FAMILY 用户选择项")
public class FamilyUserOptionVO implements Serializable {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("是否已绑定家属档案")
    private Boolean bound;

    @ApiModelProperty("已绑定家属档案ID")
    private Long boundFamilyProfileId;

    @ApiModelProperty("已绑定家属档案姓名")
    private String boundFamilyProfileName;
}
