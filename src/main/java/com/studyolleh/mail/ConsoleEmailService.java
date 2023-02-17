package com.studyolleh.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local") // 로컬환경으로 할건지 !
@Component
public class ConsoleEmailService implements EmailService{

    @Override
    public void sendEmail(EmailMessage emailMessage) {
      log.info("sent email: {}", emailMessage.getMessage());
    }
}
