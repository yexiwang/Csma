package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice//组合了@ControllerAdvice和@ResponseBody的功能。用于全局处理控制器层的异常、数据绑定和数据预处理
//@Slf4j
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler//@ExceptionHandler用于处理控制器方法抛出的异常。
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常--用户名(username)重复   处理后控制台就不会有这个错误信息，因为这个错误信息已经处理了，我们给了前台
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        return Result.error(resolveDataIntegrityMessage(ex));
    }

    @ExceptionHandler
    public Result exceptionHandler(DuplicateKeyException ex) {
        return Result.error(resolveDataIntegrityMessage(ex));
    }

    @ExceptionHandler
    public Result exceptionHandler(DataIntegrityViolationException ex) {
        return Result.error(resolveDataIntegrityMessage(ex));
    }

    private String resolveDataIntegrityMessage(Throwable throwable) {
        String message = extractRootMessage(throwable);
        if (message == null || message.trim().isEmpty()) {
            return MessageConstant.UNKNOWN_ERROR;
        }
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            if (split.length > 2) {
                return split[2] + MessageConstant.ALREADY_EXISTS;
            }
            return "数据已存在";
        }
        if (message.contains("Data too long for column")) {
            String column = extractBetween(message, "Data too long for column '", "'");
            return column == null ? "字段长度超出限制" : column + "长度超出限制";
        }
        if (message.contains("cannot be null")) {
            String column = extractBetween(message, "Column '", "'");
            return column == null ? "必填字段不能为空" : column + "不能为空";
        }
        return message;
    }

    private String extractRootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current != null ? current.getMessage() : null;
    }

    private String extractBetween(String message, String prefix, String suffix) {
        int start = message.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        start += prefix.length();
        int end = message.indexOf(suffix, start);
        if (end < 0) {
            return null;
        }
        return message.substring(start, end);
    }

}
