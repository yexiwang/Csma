package com.sky.controller.admin;

import com.sky.dto.VolunteerDTO;
import com.sky.dto.VolunteerPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.VolunteerService;
import com.sky.vo.VolunteerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminVolunteerController")
@RequestMapping("/admin/volunteer")
@Slf4j
@Api(tags = "管理端志愿者接口")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @GetMapping("/page")
    @ApiOperation("志愿者分页查询")
    public Result<PageResult> page(VolunteerPageQueryDTO volunteerPageQueryDTO) {
        log.info("志愿者分页查询，参数：{}", volunteerPageQueryDTO);
        return Result.success(volunteerService.pageQuery(volunteerPageQueryDTO));
    }

    @PostMapping
    @ApiOperation("新增志愿者")
    public Result save(@RequestBody VolunteerDTO volunteerDTO) {
        log.info("新增志愿者，数据：{}", volunteerDTO);
        volunteerService.save(volunteerDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑志愿者")
    public Result update(@RequestBody VolunteerDTO volunteerDTO) {
        log.info("编辑志愿者，数据：{}", volunteerDTO);
        volunteerService.update(volunteerDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用志愿者")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("志愿者目标状态：{} 志愿者id：{}", status, id);
        volunteerService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询志愿者信息")
    public Result<VolunteerVO> getById(@PathVariable Long id) {
        log.info("根据id查询志愿者信息：{}", id);
        return Result.success(volunteerService.getById(id));
    }

    @GetMapping("/list")
    @ApiOperation("查询可分配志愿者列表")
    public Result<List<VolunteerVO>> list() {
        log.info("query active volunteers for dispatch");
        return Result.success(volunteerService.listActiveVolunteers());
    }
}
