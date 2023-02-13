package com.studyolleh.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)             // account를 어디서 어떻게 꺼내는것인지.
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
// @authenticationPrincipal. 핸들러 매개변수로 현재 인증된 Principal을 참조할 수 있다.
// 이 principal은 인증할때 authentication에 들어있는 첫번째 파라미터.
// Service에 있는 account.getNickname() 이게 principal.
// 로그인 안한상태, 즉 인증을 안한상태에서 접근하는 경우에는 authentication의 principal이  anonymousUser가 됨.
// anonymousUser면 null. 아니면 account를 주겠다는말.
public @interface CurrentUser { //이 애너테이션이 붙은 객체의 인증된 사용자 정보가 anonymousUser라면 그 객체에는 null이 들어가고, 인증된 사용자가 있는 경우에는 account라는 프로퍼티를 뽑아온다.
                // account라는 프로퍼티를 가지고있는 중간 객체가 필요한다. UserAccount 생성.
}
