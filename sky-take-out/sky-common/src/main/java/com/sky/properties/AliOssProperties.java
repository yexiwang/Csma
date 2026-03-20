package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")//告诉Spring容器，这个类对应着application.yml中的sky.alioss开头的属性
@Data
/**
 * 这是一个配置属性类，用于从配置文件（如application.yml）中读取阿里云OSS相关的配置信息
 */
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
