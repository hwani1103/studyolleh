package com.studyolleh.mail;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Profile("dev") // dev환경으로 할건지 !
@Component // 빈을 주입받아야 하기 때문에 얘도 빈이 되어야 함.
@RequiredArgsConstructor
@Slf4j
public class HtmlEmailService implements EmailService{

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();               //멀티파트는 첨부파일을 사용할거면 true
        try {       // MimeMessageHelper로 감싸서 작업.
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), true);// html메일이면 true로.
            javaMailSender.send(mimeMessage);
            log.info("sent email: {}", emailMessage.getMessage());
        } catch (MessagingException e) {
            log.error("failed to send email", e);
        }
    }
}
