package com.njtechstation.service;

import com.njtechstation.domain.User;

public interface UserService {

    String queryPassword(String phone);

    int insertUser(User user);

    String queryNameByEmail(String email);

    int insertEmailByphone(String eamil, String phone);

    String queryMailByPhone(String phone);

    User queryUserByPhone(String phone);
}
