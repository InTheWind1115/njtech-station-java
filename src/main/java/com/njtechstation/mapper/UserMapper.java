package com.njtechstation.mapper;

import com.njtechstation.domain.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    String selectPassword(String phone);

    String selectNameByEmail(String email);

    int updateEmailByPhone(String email, String phone);

    String selectMailByPhone(String phone);
}