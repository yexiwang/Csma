package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义填充切面，用于实现公共字段自动填充
 */
@Aspect//表示该类是一个切面类，用于实现数据填充功能
@Component//表示该类是一个组件类，会被Spring容器管理
//@Slf4j//用于打印日志
public class AutoFillAspect {

    private static final Logger log = LoggerFactory.getLogger(AutoFillAspect.class);

    /**
     * 切入点
     * 1. execution 代表所要执行的表达式
     * 2. * 代表任意返回值
     * 3. .. 代表参数列表
     * 4. com.sky.mapper.*.*(..) 代表 com.sky.mapper包下的所有类中的所有方法
     * 5. @annotation(com.sky.annotation.AutoFill) 代表有这个注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPoint() {
    }

    /**
     * 前置通知，在方法执行前进行数据填充
     */
    @Before("autoFillPoint()")//@Before 表示在方法执行前执行 参数表示切入点
    public void autoFill(JoinPoint joinPoint) {
        //从被拦截方法参数中获取实体对象（如Employee）
        log.info("进行公共字段自动填充...");
        // 获取当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取到当前被拦截的方法参数---实体对象(employee)
        Object[] args = joinPoint.getArgs();//获取方法所有参数
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        log.info("当前被拦截数据对象：{}", entity);

        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据对应的数据库操作类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT) {
            //为四个公共字段赋值
            try {
                //通过反射获取对应的方法  为四个公共字段赋值
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对应的属性赋值
                setUpdateTime.invoke(entity, now);
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (operationType == OperationType.UPDATE) {
            //为两个公共字段赋值
            try {
                //通过反射获取对应的方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对应的属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
