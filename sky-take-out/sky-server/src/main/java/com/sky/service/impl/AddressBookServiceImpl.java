package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 条件查询
     *
     * @param addressBook 查询条件
     * @return 地址列表
     */
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    /**
     * 新增地址
     *
     * @param addressBook 地址信息
     */
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据地址 id 查询地址
     *
     * @param id 地址 id
     * @return 当前用户拥有的地址
     */
    public AddressBook getById(Long id) {
        return getOwnedAddressById(id);
    }

    /**
     * 根据 id 修改地址
     *
     * @param addressBook 地址信息
     */
    public void update(AddressBook addressBook) {
        getOwnedAddressById(addressBook.getId());
        addressBookMapper.update(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook 地址信息
     */
    @Transactional
    public void setDefault(AddressBook addressBook) {
        getOwnedAddressById(addressBook.getId());

        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据 id 删除地址
     *
     * @param id 地址 id
     */
    public void deleteById(Long id) {
        getOwnedAddressById(id);
        addressBookMapper.deleteById(id);
    }

    private AddressBook getOwnedAddressById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        Long currentUserId = BaseContext.getCurrentId();
        if (addressBook == null || currentUserId == null || !currentUserId.equals(addressBook.getUserId())) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        return addressBook;
    }
}
