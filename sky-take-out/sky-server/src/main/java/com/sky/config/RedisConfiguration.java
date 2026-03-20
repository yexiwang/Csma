package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration//标记该类为Spring配置类  类中的@Bean方法会被Spring容器管理
//@Slf4j
public class RedisConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean//创建RedisTemplate对象
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建Redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象  连接工厂对象RedisConnectionFactory并不用我们创建，Spring会自动创建，因为引用了spring-boot-starter-data-redis
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis的key的序列化器 用于将Redis"键"序列化为字符串格式存储和读取
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置redis的value的序列化器
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        //设置hash的key序列化器 解决hashOperations操作时的乱码问题
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //设置hash的value序列化器
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
