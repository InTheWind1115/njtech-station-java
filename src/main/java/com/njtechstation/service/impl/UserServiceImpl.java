package com.njtechstation.service.impl;

import com.njtechstation.domain.User;
import com.njtechstation.mapper.UserMapper;
import com.njtechstation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Override
    public String queryPassword(String phone) {
        return  userMapper.selectPassword(phone);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertSelective(user);
    }

    @Override
    public String queryNameByEmail(String email) {
        return userMapper.selectNameByEmail(email);
    }

    @Override
    public int insertEmailByphone(String email, String phone) {
        return userMapper.updateEmailByPhone(email, phone);
    }

    @Override
    public String queryMailByPhone(String phone) {
        return userMapper.selectMailByPhone(phone);
    }

    @Override
    public User queryUserByPhone(String phone) {
        return userMapper.selectUserByPhone(phone);
    }

}
