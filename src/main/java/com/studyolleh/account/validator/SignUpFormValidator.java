package com.studyolleh.account.validator;

import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * 커스텀 검증. @InitBinder와 WebDataBinder를 통해, 스프링이 자동으로 supports와 validate()를 호출해준다.
 */
@Component // 밸리데이터. 밸리데이터를 꼭 빈으로 등록해야 하는 건 아니지만 Bean을 주입받으려면 얘도 빈으로 등록해야 함.
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository; //레포지토리를 빈으로 주입받음.

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class); //부트가 호출해준다. 검증하려는 검증객체가 SignUpForm클래스인지. 검증.
    }

    @Override
    public void validate(Object target, Errors errors) { //target은 ModelAttribute가 전해주는 객체. 타입을 모르니까 Object로 받아서 형변환
        SignUpForm signUpForm = (SignUpForm) target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){  // 그 객체의 이메일을 얻어온다음, 이메일이 DB에 있는지 확인. Data JPA로. 이게 true라면 중복.
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }     // 리젝트밸류. 필드는 email, 에러코드는 invalid.email, 세번쨰는 에러? 배열? 에러가 한개가 아닐수도 있으니 오브젝트배열, 여기는 메세지.

        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }

    }
}
