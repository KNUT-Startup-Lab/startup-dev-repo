package com.startup.campusmate.User.repository;

import com.startup.campusmate.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentNum(String studentNum);
}
