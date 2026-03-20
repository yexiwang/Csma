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

    // 关联的 FAMILY 用户 ID
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

    // 所属助餐点 ID
    private Long diningPointId;

    // 所属网格/片区
    private String gridCode;

    // 健康状况
    private String healthInfo;

    // 特殊需求
    private String specialNeeds;

    // 身份证号
    private String idCard;

    // 照片
    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 逻辑删除标记 0未删除 1已删除
    private Integer isDeleted;
}
