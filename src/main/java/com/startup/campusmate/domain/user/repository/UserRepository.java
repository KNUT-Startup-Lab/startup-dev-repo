package com.startup.campusmate.domain.user.repository;

import com.startup.campusmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentNum(String studentNum);
}
