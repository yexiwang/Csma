package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "家属档案新增或编辑时传递的数据模型")
public class FamilyProfileDTO implements Serializable {

    @ApiModelProperty("家属档案ID")
    private Long id;

    @ApiModelProperty("关联的 FAMILY 用户ID")
    private Long userId;

    @ApiModelProperty("是否同步创建新的FAMILY账号")
    private Boolean createUser;

    @ApiModelProperty("新创建的FAMILY账号用户名")
    private String username;

    @ApiModelProperty("新创建的FAMILY账号密码")
    private String password;

    @ApiModelProperty("家属姓名")
    private String name;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("状态 1启用 0停用")
    private Integer status;
}
