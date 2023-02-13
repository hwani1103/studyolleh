package com.studyolleh.account;

import com.studyolleh.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


/**
 * 스프링 Data JPA 기능.
 */
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    Account findByEmail(String s);

    Account findByNickname(String nickname);
}
