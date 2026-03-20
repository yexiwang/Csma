package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
//@Slf4j
public class AddressBookController {

    private static final Logger log = LoggerFactory.getLogger(AddressBookController.class);

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     * 无参数
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        log.info("查询当前登录用户所有地址信息: {}",userId);
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 新增地址
     *
     * @param addressBook  Body参数
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")// "/user/addressBook"
    public Result<AddressBook> save(@RequestBody AddressBook addressBook) {
        log.info("新增地址: {}",addressBook);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 根据地址id查询地址
     * @param id  Path参数  地址id
     * @return
     */
    @GetMapping("/{id}")//实际上前端可能会发送过来http://localhost:8080/user/addressBook/2?id=2  第一个 2（路径中）：有效，被 @PathVariable 使用，第二个 2（query 参数）：冗余，是 Swagger 工具的 bug 或特性，不影响实际运行
    @ApiOperation("根据id查询地址")// "/user/addressBook/{id}"
    public Result<AddressBook> getById(@PathVariable Long id) {
        log.info("根据id查询地址: {}",id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook  Body参数  地址id变成必须
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")//  "/user/addressBook"
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("根据id修改地址: {}",addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook  Body参数  地址id
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址: {}",addressBook);
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    /**
     * 根据地址id删除地址
     *
     * @param id  Query参数
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")// "/user/addressBook?id=101"
    public Result deleteById(Long id) {
        log.info("根据id删除地址: {}",id);
        addressBookService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询默认地址  后续用户下单后才调用这个自动选择地址
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("查询默认地址: {}",addressBook);
        List<AddressBook> list = addressBookService.list(addressBook);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }

}
