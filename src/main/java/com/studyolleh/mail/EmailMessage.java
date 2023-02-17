package com.studyolleh.mail;


import lombok.Builder;
import lombok.Data;

// 실제 보낼 이메일의 폼객체라고 보면 될듯.
@Data @Builder
public class EmailMessage {

    private String to;
    private String subject;
    private String message;

}
