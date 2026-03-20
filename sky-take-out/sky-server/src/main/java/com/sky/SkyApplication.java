package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication//启动类
@EnableTransactionManagement //开启注解方式的事务管理   DishServiceImpl.java里的@Transactiona就用到了
@EnableCaching //开启缓存注解功能
@EnableScheduling//开启定时任务功能
@Slf4j
public class SkyApplication {

    //private static final Logger log = LoggerFactory.getLogger(SkyApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
