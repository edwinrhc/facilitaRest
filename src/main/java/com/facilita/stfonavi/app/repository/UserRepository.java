package com.facilita.stfonavi.app.repository;

import com.facilita.stfonavi.app.dto.user.UserListDTO;
import com.facilita.stfonavi.app.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT new com.facilita.stfonavi.app.dto.user.UserListDTO(u.id,u.name,u.email,u.contactNumber,u.role,u.status) FROM User u")
    List<UserListDTO> listUsers();


    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = :name, u.contactNumber = :contactNumber WHERE u.id = :id")
    Integer updateUserInfo(@Param("name") String name,
                           @Param("contactNumber") String contactNumber,
                           @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    Integer updateUserStatus(@Param("status") String status,
                             @Param("id") Integer id);


    @Query("SELECT u.email FROM User u WHERE u.role = 'admin'")
    List<String> getAllAdminEmails();

    User findByEmail(String email);

}
