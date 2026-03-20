package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云文件上传配置类  用于创建AliOssUtil对象
 */
@Configuration//表明这是一个Web相关的配置类，用于注册web层组件如拦截器、资源处理器等
//@Slf4j
public class OssConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OssConfiguration.class);

    @Bean//当项目启动的时候就会调用下面方法 创建AliOssUtil对象
    @ConditionalOnMissingBean//当容器中没有这个对象时，才会创建这个对象     但其实这个注解也不用加，因为configuration默认单实例,有实例后就不会创建了，不需要condition
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云文件上传工具类对象:{}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }

}
