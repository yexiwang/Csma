package com.sky.controller.admin;

import com.sky.dto.ElderlyDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ElderlyService;
import com.sky.vo.ElderlyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/elderly")
@Api(tags = "老人档案管理接口")
public class ElderlyController {

    private static final Logger log = LoggerFactory.getLogger(ElderlyController.class);

    @Autowired
    private ElderlyService elderlyService;

    @PostMapping
    @ApiOperation("新增老人档案")
    public Result save(@RequestBody ElderlyDTO elderlyDTO) {
        log.info("新增老人档案：{}", elderlyDTO);
        elderlyService.save(elderlyDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("老人档案分页查询")
    public Result<PageResult> page(int page, int pageSize, String name, Long diningPointId) {
        log.info("老人档案分页查询：page={}, pageSize={}, name={}, diningPointId={}", page, pageSize, name, diningPointId);
        return Result.success(elderlyService.pageQuery(page, pageSize, name, diningPointId));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询老人档案")
    public Result<ElderlyVO> getById(@PathVariable Long id) {
        log.info("根据id查询老人档案：{}", id);
        return Result.success(elderlyService.getById(id));
    }

    @PutMapping
    @ApiOperation("修改老人档案")
    public Result update(@RequestBody ElderlyDTO elderlyDTO) {
        log.info("修改老人档案：{}", elderlyDTO);
        elderlyService.update(elderlyDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除老人档案")
    public Result delete(@PathVariable Long id) {
        log.info("删除老人档案：{}", id);
        elderlyService.deleteById(id);
        return Result.success();
    }
}
