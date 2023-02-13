package com.studyolleh.config;

import com.studyolleh.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up",
                        "/check-email-token", "/email-login", "/check-email-login","/login-by-email",
                        "/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();

        //http.formLogin()까지만 해도 스프링 시큐리티가 제공하는 기본 로그인 view가 생성된다.
        //loginPage메서드를 사용하여 스스로 만든 Login폼을 보여주기 위해 맵핑을 한다.
        //permitAll()까지 해서 시큐리티 검증을 통과시킨다.
        http.formLogin()
                .loginPage("/login").permitAll();
        //logout도 마찬가지로 추가 메서드는 필요 없지만, logoutSuccessUrl메서드는 로그아웃 후 보여줄 화면을 정할 수 있다.
        http.logout()
                .logoutSuccessUrl("/");

        //리멤버미 쿠키, 로그인 유지. JSESSIONID 쿠키를 삭제해도 리멤버미 쿠키가 남아있으면 로그인이 유지된다. JSESSIONID쿠키는 계속생성됨.
        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());

    }

    @Bean //리멤버미 토큰.관련. 리포지토리.
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()                          //npm 사용설정때문에 추가함.
                .mvcMatchers("/node_modules/**") // < .mvcMatchers("/node/modeuls/**)이렇게 추가함. 추가하니까 잘 되네. static쪽에 시큐리티 검증 안하게 처리하는방법!
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}

//package com.studyolle.config;
//
//        import com.studyolle.account.AccountService;
//        import lombok.RequiredArgsConstructor;
//        import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
//        import org.springframework.context.annotation.Bean;
//        import org.springframework.context.annotation.Configuration;
//        import org.springframework.http.HttpMethod;
//        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//        import org.springframework.security.config.annotation.web.builders.WebSecurity;
//        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//        import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//        import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
//        import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
//
//        import javax.sql.DataSource;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    private final AccountService accountService;
//    private final DataSource dataSource;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
//                        "/email-login", "/login-by-email").permitAll()
//                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
//                .anyRequest().authenticated();
//
//        http.formLogin()
//                .loginPage("/login").permitAll();
//
//        http.logout()
//                .logoutSuccessUrl("/");
//
//        http.rememberMe()
//                .userDetailsService(accountService)
//                .tokenRepository(tokenRepository());
//    }
//
//    @Bean
//    public PersistentTokenRepository tokenRepository() {
//        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
//        jdbcTokenRepository.setDataSource(dataSource);
//        return jdbcTokenRepository;
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring()
//                .mvcMatchers("/node_modules/**")
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    }
//}
