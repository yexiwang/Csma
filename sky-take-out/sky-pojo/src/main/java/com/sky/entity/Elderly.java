package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Elderly implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // 关联用户ID(家属或本人)
    private Long userId;

    // 老人姓名
    private String name;

    // 性别
    private String gender;

    // 年龄
    private Integer age;

    // 联系电话
    private String phone;

    // 详细居住地址
    private String address;

    // 所属助餐点ID/默认助餐点ID，表示该老人默认由哪个助餐点提供助餐服务
    private Long diningPointId;

    // 所属网格/片区
    private String gridCode;

    // 健康状况(慢性病/过敏源)
    private String healthInfo;

    // 特殊需求
    private String specialNeeds;

    // 身份证号
    private String idCard;

    // 照片
    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
