package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "家属档案返回对象")
public class FamilyProfileVO implements Serializable {

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

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("状态 1启用 0停用")
    private Integer status;

    @ApiModelProperty("已关联老人数量")
    private Integer elderlyCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private Long createUser;

    @ApiModelProperty("修改人")
    private Long updateUser;
}
