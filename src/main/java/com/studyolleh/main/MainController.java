package com.studyolleh.main;

import com.studyolleh.account.CurrentUser;
import com.studyolleh.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/") //CurrentUser애너테이션을 만들어서. 동적으로, 로그인 했으면 account객체, 안했으면 null로 받고싶다.
    public String home(@CurrentUser Account account, Model model){
        if(account != null){
            model.addAttribute(account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(){

        return "login";
    }
}
