package com.studyolleh.main;

import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest // 스프링 부트 테스트에는 이걸 붙여줘야함.
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc; // junit5가 디펜던시 인젝션을 지원하는데, RequiredArgsConstructor로는 주입 안됨.
    //Autowired를 써야됨.
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;


    @BeforeEach
    void beforeEach(){

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("keesun");
        signUpForm.setEmail("keesun@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }




    @DisplayName("이메일로 로그인 성공")
    @Test // 스프링 시큐리티에서 username, password는 정해진 키워드임. 해당 의미로 사용됨.
    void login_with_email() throws Exception{

        mockMvc.perform(post("/login")
                .param("username", "keesun@email.com")
                .param("password", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("keesun"));
    }

    @DisplayName("넥네임으로 로그인 성공")
    @Test
    void login_with_nickname() throws Exception{
        mockMvc.perform(post("/login")
                        .param("username", "keesun")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("keesun"));
    }

    @DisplayName("로그인 실패")
    @Test // 스프링 시큐리티에서 username, password는 정해진 키워드임. 해당 의미로 사용됨.
    void login_fail() throws Exception{
        mockMvc.perform(post("/login")
                        .param("username", "12312")
                        .param("password", "123451233678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception{
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}