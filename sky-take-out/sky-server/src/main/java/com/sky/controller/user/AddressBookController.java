package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的全部地址")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("查询当前登录用户全部地址，userId={}", addressBook.getUserId());
        return Result.success(addressBookService.list(addressBook));
    }

    @PostMapping
    @ApiOperation("新增地址")
    public Result<AddressBook> save(@RequestBody AddressBook addressBook) {
        log.info("新增地址：{}", addressBook);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        log.info("根据 id 查询地址：{}", id);
        return Result.success(addressBookService.getById(id));
    }

    @PutMapping
    @ApiOperation("根据 id 修改地址")
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("根据 id 修改地址：{}", addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("根据 id 删除地址")
    public Result deleteById(Long id) {
        log.info("根据 id 删除地址：{}", id);
        addressBookService.deleteById(id);
        return Result.success();
    }

    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        AddressBook query = new AddressBook();
        query.setIsDefault(1);
        query.setUserId(BaseContext.getCurrentId());
        log.info("查询默认地址：{}", query);
        List<AddressBook> list = addressBookService.list(query);
        if (list == null || list.isEmpty()) {
            return Result.success();
        }
        return Result.success(list.get(0));
    }
}
