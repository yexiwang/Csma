package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component//注解表示这个类是一个组件，会被Spring容器管理
@ConfigurationProperties(prefix = "sky.jwt")//告诉Spring容器，这个类对应的是application.yml中的sky.jwt开头的属性
@Data//自动生成getter和setter方法
public class JwtProperties {//封装jwt令牌相关属性

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;//密钥
    private long adminTtl;//有效期
    private String adminTokenName; //令牌名称

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
