package com.startup.campusmate.domain.member.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendMailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, String resetUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("비밀번호 재설정을 위한 링크 안내");

        String content = "<p>안녕하세요.</p>"
                + "<p>아래 링크를 클릭하여 비밀번호를 재설정해 주세요. (유효시간: 1시간)</p>"
                + "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>"
                + "<br><p>감사합니다.</p>";
        helper.setText(content, true);

        mailSender.send(message);
    }
}


