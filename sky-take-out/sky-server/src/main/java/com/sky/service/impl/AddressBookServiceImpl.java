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

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    @Override
    public void save(AddressBook addressBook) {
        Long currentUserId = BaseContext.getCurrentId();
        addressBook.setUserId(currentUserId);

        AddressBook query = new AddressBook();
        query.setUserId(currentUserId);
        List<AddressBook> addressList = addressBookMapper.list(query);
        addressBook.setIsDefault(addressList == null || addressList.isEmpty() ? 1 : 0);

        addressBookMapper.insert(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        return getOwnedAddressById(id);
    }

    @Override
    public void update(AddressBook addressBook) {
        getOwnedAddressById(addressBook.getId());
        addressBookMapper.update(addressBook);
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        getOwnedAddressById(addressBook.getId());

        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
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
