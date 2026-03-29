package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@ApiModel(description = "C端登录时传递的数据模型")
public class UserLoginDTO implements Serializable {
    @ApiModelProperty("历史兼容字段，当前不再支持微信 code 登录")
    private String code;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;
}
