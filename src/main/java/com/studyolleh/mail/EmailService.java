package com.studyolleh.mail;

//로컬 / 운영환경에 따라 이메일을 다르게 보내게 하기 위해 추상화 인터페이스를 작성.
public interface EmailService {

    void sendEmail(EmailMessage emailMessage);

}
