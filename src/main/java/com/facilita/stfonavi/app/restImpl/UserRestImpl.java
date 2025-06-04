package com.facilita.stfonavi.app.restImpl;


import com.facilita.stfonavi.app.constants.AuthConstants;
import com.facilita.stfonavi.app.dto.user.*;
import com.facilita.stfonavi.app.rest.UserRest;
import com.facilita.stfonavi.app.service.UserService;
import com.facilita.stfonavi.app.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserRestImpl implements UserRest {

    @Autowired
    UserService userService;



    @Override
    public ResponseEntity<String> signUp(UserDTO userDto) {
        try {
            return userService.signUp(userDto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(LoginDTO loginDTO) {
        try {
            return userService.login(loginDTO);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserListDTO>> getAllUser() {
        try {
            return userService.listUsers();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<List<UserListDTO>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> updateProfile(UserUpdateDTO dto) {
        try {
            return userService.updateUserProfile(dto);
        } catch (Exception ex) {
            ex.printStackTrace();
            return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<String> updateStatus(UserStatusDTO dto) {
        try {
            return userService.updateUserStatus(dto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            return userService.forgotPassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        try {
            return userService.resetPassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
