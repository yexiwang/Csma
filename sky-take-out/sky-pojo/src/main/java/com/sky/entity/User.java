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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 登录账号
    private String username;

    // 登录密码
    private String password;

    // 角色 (FAMILY, VOLUNTEER)
    private String role;

    // 状态 0:禁用 1:启用
    private Integer status;

    // 姓名
    private String name;

    // 手机号
    private String phone;

    // 性别 0 女 1 男
    private String sex;

    // 身份证号
    private String idNumber;

    // 头像
    private String avatar;

    // 注册时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
