package com.sky.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor//Lombok注解，用于自动生成包含所有字段的构造函数。
@NoArgsConstructor//Lombok注解，用于自动生成无参构造函数。
@ApiModel(description = "分页查询结果返回的数据模型")
public class PageResult implements Serializable {

    @ApiModelProperty("总记录数")
    private long total; //总记录数
    @ApiModelProperty("当前页的数据")
    private List records; //当前页数据集合

}
