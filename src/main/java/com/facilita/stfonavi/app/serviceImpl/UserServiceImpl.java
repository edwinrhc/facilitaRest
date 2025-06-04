package com.facilita.stfonavi.app.serviceImpl;


import com.facilita.stfonavi.app.configurtion.AppProperties;
import com.facilita.stfonavi.app.constants.AuthConstants;
import com.facilita.stfonavi.app.dto.user.*;
import com.facilita.stfonavi.app.entity.PasswordResetToken;
import com.facilita.stfonavi.app.entity.User;
import com.facilita.stfonavi.app.jwt.CustomerUsersDetailsService;
import com.facilita.stfonavi.app.jwt.JwtFilter;
import com.facilita.stfonavi.app.jwt.JwtUtil;
import com.facilita.stfonavi.app.repository.PasswordResetTokenRepository;
import com.facilita.stfonavi.app.repository.UserRepository;
import com.facilita.stfonavi.app.service.UserService;
import com.facilita.stfonavi.app.utils.AuthUtils;
import com.facilita.stfonavi.app.utils.EmailUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    EmailUtils emailUtils;

    @Autowired
    private AppProperties appProperties;


    @Override
    public ResponseEntity<String> signUp(UserDTO userDTO) {
        log.info("Trying to register User: {}", userDTO );
        try{
            User validEmail = userRepository.findByEmail(userDTO.getEmail());
            if(Objects.isNull(validEmail)) {
                User newUser = toEntity(userDTO);
                userRepository.save(newUser);
                return AuthUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
            }else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private User toEntity(UserDTO userDTO){
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setContactNumber(userDTO.getContactNumber());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole("user");
        user.setStatus("false");
        return user;
    }

    public ResponseEntity<String> login(LoginDTO loginDTO) {
        log.info("Inside login");
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            if(authentication.isAuthenticated()) {
                if(customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>("{\"token\":\"" +
                            jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                                    customerUsersDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                }else{
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval." + "\"}", HttpStatus.BAD_REQUEST);
                }
            }
        }catch (Exception ex){
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserListDTO>> listUsers() {
        try{
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userRepository.listUsers(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    userRepository.updateUserInfo(
                            requestMap.get("name"),
                            requestMap.get("contactNumber"),
                            Integer.parseInt(requestMap.get("id"))
                    );
                    List<String> requiredFields = List.of("id", "name", "contactNumber");
                    List<String> missingFields = new ArrayList<>();

                    for (String field : requiredFields) {
                        if (!requestMap.containsKey(field) || requestMap.get(field).isBlank()) {
                            missingFields.add(field);
                        }
                    }

                    if (!missingFields.isEmpty()) {

                        String mensaje = "The fllowing fields are missing: " + String.join(", ", missingFields);
                        log.warn(mensaje, HttpStatus.BAD_REQUEST);
                        return AuthUtils.getResponseEntity(mensaje, HttpStatus.BAD_REQUEST);
                    }

                    return AuthUtils.getResponseEntity("User Status Updated Successfully", HttpStatus.OK);
                } else {
                    return AuthUtils.getResponseEntity("User id doesn't not exist", HttpStatus.OK);
                }
            } else {
                return AuthUtils.getResponseEntity(AuthConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateUserStatus(UserStatusDTO userStatusDTO) {
        try{
            if(jwtFilter.isAdmin()) {

                log.info("Admin confirmado");
                log.info("Buscando ID: {}", userStatusDTO.getId());

                Optional<User> optional = userRepository.findById(userStatusDTO.getId());
                if(!optional.isEmpty()) {
                    userRepository.updateUserStatus(
                            userStatusDTO.getStatus(),
                            userStatusDTO.getId());
                    // Enviamos notificacion
                    sendMailToAllAdmin(
                            userStatusDTO.getStatus(),
                            optional.get().getEmail(),
                            userRepository.getAllAdminEmails()
                    );
                    return AuthUtils.getResponseEntity("User Status Updated Successfully", HttpStatus.OK);
                }else{
                    return AuthUtils.getResponseEntity("User id doesn't not exist", HttpStatus.OK);
                }
            }else{
                return AuthUtils.getResponseEntity(AuthConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateUserProfile(UserUpdateDTO userUpdateDTO) {
        try{
            String email = customerUsersDetailsService.getUserDetail().getEmail();
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return AuthUtils.getResponseEntity("User not found", HttpStatus.NOT_FOUND);
            }
            // Actualizamos solo el usuario autenticado
            user.setName(userUpdateDTO.getName());
            user.setContactNumber(userUpdateDTO.getContactNumber());
            userRepository.save(user);

            return AuthUtils.getResponseEntity("User profile updated successfully", HttpStatus.OK);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return AuthUtils.getResponseEntity(AuthConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Transactional
    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        User user = userRepository.findByEmail(requestMap.get("email"));
        if (user == null) {
            return AuthUtils.getResponseEntity("Email no registrado", HttpStatus.BAD_REQUEST);
        }

        // 💡 Elimina token anterior si ya existe para ese usuario
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationDate(
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(appProperties.getResetTokenExpirationMinutes()))
        );
        passwordResetTokenRepository.save(resetToken);

        System.out.println("🔍 baseUrl = " + appProperties.getBaseUrl());

        String link = appProperties.getBaseUrl()+"/user/reset-password?token=" + token;
        String html = "<h3>Restablecimiento de contraseña</h3><p>Token: <b>" + token + "</b><br>También puedes usar este link: <a href='" + link + "'>" + link + "</a></p>";

        try {
            emailUtils.sendHtmlMessage(user.getEmail(), "Restablecer contraseña - Clinic App", html, null);
        } catch (MessagingException e) {
            return AuthUtils.getResponseEntity("Error al enviar correo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return AuthUtils.getResponseEntity("Correo enviado con instrucciones", HttpStatus.OK);
    }

    @Transactional
    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        String token = requestMap.get("token").trim();
        String newPassword = requestMap.get("newPassword");

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return AuthUtils.getResponseEntity("Token no encontrado", HttpStatus.BAD_REQUEST);
        }

        Date expirationTime = tokenOpt.get().getExpirationDate();
        Date now = new Date();

        if (expirationTime.before(now)) {
            return AuthUtils.getResponseEntity("Token expirado", HttpStatus.BAD_REQUEST);
        }

        User user = tokenOpt.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
//        passwordResetTokenRepository.deleteByToken(token);
        passwordResetTokenRepository.delete(tokenOpt.get());

        return AuthUtils.getResponseEntity("Contraseña actualizada correctamente", HttpStatus.OK);
    }


    public void sendMailToAllAdmin(String status, String userEmail, List<String> adminEmails) {
        String adminEmail = jwtFilter.getCurrentUser();

        String subject = "✔ Usuario " + userEmail + " ha sido " + status.toUpperCase();

        String htmlMsg = "<h2 style='color:#2e6da4;'>🔔 Actualización de estado de usuario</h2>" +
                "<p><b>👤 Usuario:</b> " + userEmail + "<br>" +
                "<b>🔁 Nuevo estado:</b> <span style='color:" + (status.equalsIgnoreCase("approved") ? "green" : "red") + ";'>" + status.toUpperCase() + "</span><br>" +
                "<b>👮‍♂️ Aprobado por:</b> " + adminEmail + "<br>" +
                "<b>📅 Fecha:</b> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</p>";

        try {
            emailUtils.sendHtmlMessage(adminEmail, subject, htmlMsg, adminEmails);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }




}
