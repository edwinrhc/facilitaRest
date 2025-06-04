package com.facilita.stfonavi.app.service;

import com.facilita.stfonavi.app.dto.user.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {


    ResponseEntity<String> signUp(UserDTO userDTO);

    ResponseEntity<String> login(LoginDTO loginDTO);

    ResponseEntity<List<UserListDTO>> listUsers();


    ResponseEntity<String> updateUserProfile(UserUpdateDTO userUpdateDTO);
    ResponseEntity<String> updateUserStatus(UserStatusDTO userStatusDTO);


    ResponseEntity<String> updateUser(Map<String,String> requestMap);

    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
    ResponseEntity<String> resetPassword(Map<String, String> requestMap);



}