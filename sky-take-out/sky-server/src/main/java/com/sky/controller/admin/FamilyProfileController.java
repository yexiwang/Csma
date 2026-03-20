package com.sky.controller.admin;

import com.sky.dto.FamilyProfileDTO;
import com.sky.dto.FamilyProfilePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.FamilyProfileService;
import com.sky.vo.FamilyProfileOptionVO;
import com.sky.vo.FamilyProfileVO;
import com.sky.vo.FamilyUserOptionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/familyProfile")
@Slf4j
@Api(tags = "管理员端家属档案接口")
public class FamilyProfileController {

    @Autowired
    private FamilyProfileService familyProfileService;

    @GetMapping("/page")
    @ApiOperation("家属档案分页查询")
    public Result<PageResult> page(FamilyProfilePageQueryDTO familyProfilePageQueryDTO) {
        log.info("家属档案分页查询：{}", familyProfilePageQueryDTO);
        return Result.success(familyProfileService.pageQuery(familyProfilePageQueryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询家属档案")
    public Result<FamilyProfileVO> getById(@PathVariable Long id) {
        log.info("根据ID查询家属档案：{}", id);
        return Result.success(familyProfileService.getById(id));
    }

    @PostMapping
    @ApiOperation("新增家属档案")
    public Result save(@RequestBody FamilyProfileDTO familyProfileDTO) {
        log.info("新增家属档案：{}", familyProfileDTO);
        familyProfileService.save(familyProfileDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改家属档案")
    public Result update(@RequestBody FamilyProfileDTO familyProfileDTO) {
        log.info("修改家属档案：{}", familyProfileDTO);
        familyProfileService.update(familyProfileDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用或停用家属档案")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("更新家属档案状态：status={}, id={}", status, id);
        familyProfileService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除家属档案")
    public Result delete(@RequestParam Long id) {
        log.info("删除家属档案：{}", id);
        familyProfileService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/options")
    @ApiOperation("查询可选家属档案")
    public Result<List<FamilyProfileOptionVO>> options() {
        log.info("查询可选家属档案");
        return Result.success(familyProfileService.listEnabledOptions());
    }

    @GetMapping("/familyUsers")
    @ApiOperation("查询全部FAMILY用户及绑定状态")
    public Result<List<FamilyUserOptionVO>> familyUsers() {
        log.info("查询全部FAMILY用户及绑定状态");
        return Result.success(familyProfileService.listFamilyUsers());
    }
}
