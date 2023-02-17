package com.studyolleh;

import com.studyolleh.account.AccountService;
import com.studyolleh.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor // 이부분 잘 모르겠으면 나중에 프로필 수정 테스트 강좌 다시 보자.
//일단 기능은 WithAccount 커스텀 애너테이션이랑 같이, 단위 테스트 시작하기 전에 얘가 먼저 시작돼서
//인증된 Authentication principal객체를 만들어주는 기능임.
public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {
    private final AccountService accountService;

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        String nickname = withAccount.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname + "@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);

        UserDetails principal = accountService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
