package com.studyolleh.settings;

import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.form.SignUpForm;
import com.studyolleh.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTestOriginal {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

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


    @WithUserDetails(value = "keesun", setupBefore =
            TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        //이 url 자체가 인증된 사용자만 접근 가능하기 때문에 다른 검증은 간단함.
        // @WithUserDetails가 없다면 테스트는 엄청 어려워진다. 로그인 여부를 어떻게 검증
    }







    // 강의에서는 대안으로 @WithAccount를 알려줌. 뭐 상속받고 그 안에서 객체까지 만들고 애너테이션 만들고 해야된다.
    // 아주 좋은 기능이라고는 하는데 일단은 지금 버그가 고쳐져서 아래의 애너테이션으로 정상 작동이 되니까, 알고만 있자

    // 강의에서는 @WithUserDetails 이게 버그가 있어서 작동이 안된다고했는데, 요즘엔 고쳐진것같다.실행이 일단 된다.
    // keesun이라는 값을 받아온다음에 해당하는 데이터를 읽어서 Account 엔티티객체를 시큐리티 컨텍스트에 넣어준다 WithUserDetails가.
    @WithUserDetails(value = "keesun", setupBefore = TestExecutionEvent.TEST_EXECUTION) //이걸 해줘야 함. 이거는 BeforeEach보다는 나중에, 테스트보다는 먼저 실행됨.
//    @WithUserDetails("keesun") 에러. BeforeEach보다 WithUserDetails가 먼저 작동됨.
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertEquals(bio, keesun.getBio());
    }


    @WithUserDetails(value = "keesun", setupBefore = TestExecutionEvent.TEST_EXECUTION) //이걸 해줘야 함. 이거는 BeforeEach보다는 나중에, 테스트보다는 먼저 실행됨.
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception{
        String bio = "짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우. " +
                "짧은 소개를 수정하는 경우. " +
                "짧은 소개를 수정하는 경우. " +
                "짧은 소개를 수정하는 경우. " +
                "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());


        Account keesun = accountRepository.findByNickname("keesun");
        assertNull(keesun.getBio());


    }


    @WithUserDetails(value = "keesun", setupBefore =
            TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception{
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "keesun", setupBefore =
            TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력 값 정상")
    @Test
    void updatePassword_success() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));
        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(passwordEncoder.matches("12345678", keesun.getPassword()));
    }


    @WithUserDetails(value = "keesun", setupBefore =
            TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력 값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception{
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

//테스트 단위를 조금 크게 보자. 컨트롤러부터 하나의 기능 단위로 보고
    //얽혀있는 서비스 리포지토리를 한번에 다 테스트.
    // 컨트롤러부터 테스트하다보면 경우의수가 많아질 수 있다.



}