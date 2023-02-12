package com.studyolleh.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Data
public class SignUpForm {

    @NotBlank(message = "공백은 허용되지 않습니다.")
    @Length(message = " 길이는 최소 3글자에서 최대 20글자 사이입니다.", min = 3, max = 20)
    @Pattern(message = " 특수문자는 _, - 만 허용됩니다.", regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 8, max = 50)
    private String password;


}