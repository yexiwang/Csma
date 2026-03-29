package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "FAMILY";

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        if (StringUtils.hasText(userLoginDTO.getCode())) {
            throw new LoginFailedException("当前系统已停用微信登录，请使用账号密码登录");
        }

        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByUsername(username);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        if (!StringUtils.hasText(user.getRole())) {
            user.setRole(DEFAULT_ROLE);
        }
        return user;
    }

    @Override
    public void register(User user) {
        user.setStatus(1);
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole(DEFAULT_ROLE);
        }

        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        } else {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }
}
