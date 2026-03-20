package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {//MultipartFile: 文件上传接口   file: 文件(这个参数名称不能乱写，要和前端的请求一样)
        log.info("文件上传：{}", file);
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            log.info("原始文件名：{}", originalFilename);
            //获取原始文件名的后缀  如 .jpg、.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成新文件名  防止文件重名导致的覆盖
            String objectName = UUID.randomUUID().toString() + extension;


            // 调用阿里云OSS工具类上传文件 filePath=上传成功后的文件访问路径  objectName是我们传上去的文件在阿里云上的名字
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("文件上传成功，访问路径：{}", filePath);
            return Result.success(filePath);//返回上传成功后的文件访问路径  用于前端根据这个路径访问文件来显示图片
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
