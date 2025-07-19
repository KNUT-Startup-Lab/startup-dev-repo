package com.startup.campusmate.domain.member.repository;

import com.startup.campusmate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentNum(String studentNum);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNameAndPhoneNum(String name, String phoneNum);

    Member findById(Member member);
}
