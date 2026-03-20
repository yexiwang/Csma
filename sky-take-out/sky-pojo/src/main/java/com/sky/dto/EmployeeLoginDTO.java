package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data//自动为类生成getter、setter、toString、equals和hashCode方法
@ApiModel(description = "员工登录时传递的数据模型")//Swagger注解，用于给API接口添加描述信息
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("用户名")//Swagger注解，用于给属性添加描述信息
    private String username;

    @ApiModelProperty("密码")//Swagger注解，用于给属性添加描述信息
    private String password;

}
