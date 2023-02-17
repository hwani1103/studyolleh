package com.studyolleh.settings.validator;

import com.studyolleh.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// 이 밸리데이터는 다른 뭘 사용하는게 없으니까 그냥 new 해서 만들어도 됨. 빈 등록 안하고.
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz); // 어떤 타입의 폼 객체를 검증할것이냐
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
