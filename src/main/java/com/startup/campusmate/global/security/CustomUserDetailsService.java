package com.startup.campusmate.global.security;

import com.startup.campusmate.domain.member.member.entity.Member;
import com.startup.campusmate.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + email));

        return new CustomUserDetails(
                member.getId(),
                member.getUsername(),
                member.getPassword(),
                member.getNickname(),
                member.getAuthorities()
        );
    }
}


