package com.studyolleh.account;

import com.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;



    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute(new SignUpForm());

        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Validated @ModelAttribute SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);

        String view = "account/checked-email";
        if(account == null){
            model.addAttribute("error", "wrong email");
            return view;
        }
        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong token");
            return view;
        }

        accountService.completeSignUp(account);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;

    }

    @GetMapping("/check-email")  // 체크 이메일. 이메일 인증이 처리되지 않은 사용자에 대해 index 페이지에서 경고창을 보여주고, 이메일 인증을 확인하는 링크를 보여준다.
    public String checkEmail(@CurrentUser Account account, Model model){ // @CurrentUser애너테이션 이거 사기네 사기야.. 암튼 .로직은 간단함.
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model){
        if(!account.canSendConfirmEmail()){ // 엔티티에 canSendConfirmEmail메서드 추가
            model.addAttribute("error", "인증 이메일은 1시간에 한번씩만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    //자기 정보를 조작할 수 있는 사용자의 로그인인지 확인
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model,
                              @CurrentUser Account account){
        Account byNickname = accountRepository.findByNickname(nickname);
        if(nickname == null){
            throw new IllegalArgumentException(nickname + " 에 해당하는 사용자가 없습니다.");
        }

        // 요청한 nickname에 맞는 회원 객체를 DB에서 조회해온 결과.
        model.addAttribute("account", byNickname);
        // 요청 정보로 불러온 객체와 로그인 한 객체가 동일하다면 true, 아니면 false
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }


    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login"; //패스워드 없이 로그인하기 테스트하려면 이 줄 주석
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {

            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }
        accountService.login(account);
        return view;
    }



}
