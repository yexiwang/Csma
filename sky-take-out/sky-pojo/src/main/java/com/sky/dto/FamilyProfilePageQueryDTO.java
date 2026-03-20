package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "家属档案分页查询参数")
public class FamilyProfilePageQueryDTO implements Serializable {

    @ApiModelProperty("家属姓名")
    private String name;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("状态 1启用 0停用")
    private Integer status;

    @ApiModelProperty("页码")
    private int page;

    @ApiModelProperty("每页记录数")
    private int pageSize;
}
