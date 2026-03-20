package com.sky.vo;

import com.sky.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder//自动生成有参构造函数
@NoArgsConstructor//自动生成无参构造函数
@AllArgsConstructor//自动生成包含所有字段的构造函数
@ApiModel(description = "套餐返回视图对象")
public class SetmealVO implements Serializable {

    private Long id;

    //分类id
    @ApiModelProperty("分类id")
    private Long categoryId;

    //套餐名称
    @ApiModelProperty("套餐名称")
    private String name;

    //套餐价格
    @ApiModelProperty("套餐价格")
    private BigDecimal price;

    //状态 0:停用 1:启用
    @ApiModelProperty("状态 0:停用 1:启用")
    private Integer status;

    //描述信息
    @ApiModelProperty("描述信息")
    private String description;

    //图片
    @ApiModelProperty("图片")
    private String image;

    //更新时间
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    //分类名称
    @ApiModelProperty("分类名称")
    private String categoryName;

    //套餐和菜品的关联关系
    @ApiModelProperty("套餐和菜品的关联关系")
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
