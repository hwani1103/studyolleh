package com.studyolleh.account;


import com.studyolleh.domain.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

// 스프링 시큐리티 기능. User <--
public class UserAccount extends User {
//principal 객체

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
