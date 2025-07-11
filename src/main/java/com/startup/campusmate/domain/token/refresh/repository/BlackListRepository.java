package com.startup.campusmate.domain.member.repository;

import com.startup.campusmate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentNum(String studentNum);
    Optional<Member> findByEmail(String email);
}
