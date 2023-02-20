package com.studyolleh.account;

import com.studyolleh.account.form.SignUpForm;
import com.studyolleh.config.AppProperties;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Tag;
import com.studyolleh.domain.Zone;
import com.studyolleh.mail.EmailMessage;
import com.studyolleh.mail.EmailService;
import com.studyolleh.settings.form.Notifications;
import com.studyolleh.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor // UserDetailsService 구현 ! 스프링 시큐리티를 사용하면 로그인 처리는 스프링 시큐리티가 해주지만
// DB에 접근해서 실제 회원정보를 비교하는 등의 작업을 해야하기때문에 아래의 인터페이스는 구현해서 메서드를 작성해줘야 한다.
@Slf4j
public class AccountService implements UserDetailsService {
    // UserDetailsService 타입의 빈이 하나만 있으면 스프링 시큐리티 설정에 아무것도 해줄필요없음.
    // 로그인 로그아웃 전부 동작해줌.

    private final AccountRepository accountRepository;
    private final EmailService emailService; // emailService. 추상화. active profile설정에 따라 ConsoleEmailService or HtmlEmailService가 주입된다.
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
//        newAccount.generateEmailCheckToken(); // ** generateEmailCheckToken()을 꼭 여기서 해야 하는 이유? // 밑으로 내림!
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    //saveNewAccount(@Valid SignUpForm signUpForm) 에서 @Valid 안 써도 될것같아서 지움. 깃에도 있던데. 문제되면 다시 붙이자.
    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context(); // 타임리프 패키지의 Context. 모델이라고 생각하면 됨.
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);


        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)

                .build();
        emailService.sendEmail(emailMessage);


//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setTo(newAccount.getEmail());
//        mailMessage.setSubject("스터디 올래, 회원 가입 인증");
//        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
//                "&email=" + newAccount.getEmail());
//        javaMailSender.send(mailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account), // 여기서 getNickname이 있기 때문에 html에서 authentication.name으로 조회가 됐던거네.
//                account.getNickname(), // 원랜 이거였음. principal이 getNickname() 즉 String이었는데,
//                account타입으로 받고싶으니까 new UserAccount(account);
//                그리고 UserAccount가 Account account를 가지고있음.
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
        //로그인 완료 처리. 스프링 시큐리티
    }

    @Transactional(readOnly = true)
    @Override // 스프링 시큐리티의 /login post요청을 받는 메서드.
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }
        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);
    }

    //같은 Account account라도 얘는 영속상태의 객체였기때문에 따로 repository를 통한 update가 일어나지 않았어도
    //DB에 변경사항이 저장된다.
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    //이 객체는 세션에 담겨있던 객체이고 처음에는 DB에서 값을 읽어왔지만 이미 Detached되었기 때문에 변경 감지가 일어나지 않음.
    //영속성 컨텍스트가 유지되는것은 뷰를 랜더링할 떄 까지.
    //정확히 어떤 시점에 얘가 Detached가 되었는지는 모르겠지만
    //로그인이 되자 마자 생성되고 세션에 담겼으니까, 그 첫 뷰가 랜더링되고 Detached가 된 것 같다.
    //아무튼 이후에는 @CurrentUser Account account라고 가져왔을 경우 이 객체는 Detached상태
    //적어도 어떤 객체에 변경을 할 때에는 직접 repository에 접근하거나, 아니면 그 객체의 상태를 잘 파악해야 한다.
    //객체가 어떤상태인지, 트랜잭션이 유지되고 있는 상태인지 아닌지
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account); // profile에 있는걸 account로 옮기고 1:1대응이 되면 이렇게 하면 간단히
        // 간단히 값 복사가 된다.
//        account.setUrl(profile.getUrl());
//        account.setOccupation(profile.getOccupation());
//        account.setLocation(profile.getLocation());
//        account.setBio(profile.getBio());
//        account.setProfileImage(profile.getProfileImage()); // 이미지 셋.
        accountRepository.save(account);
        //현재 account객체는 Authentication의 principal 객체 account이고 그 객체의 트랜잭션은
        //진작에 끝났다. 즉 준영속(Detached) 상태임. 준영속상태의 객체는 아무리 값을 변경해도
        //영속성 컨텍스트에서 변경감지를 하지 못한다.
        // 준영속 객체의 변경을 저장하려면 다시 repository에서 save를 호출해주면 된다.
        // id값을 확인하고 merge를 시킨다. 기존 데이터에 업데이트를 시킨다. id값이 있으면(준영속 객체이면)

    }

    //여기 account도 detached 객체
    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword)); // 비밀번호 절대 그대로 저장하면 안된다.
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); // 이 로그인을 안해주면, 닉네임을 바꾼게 적용이 안됨 jdenticon떔에 그런듯? principal객체의 닉네임을 토대로 이미지 만드니까?
        // 이미지를 사진으로 등록해놨으면 상관은 없음.이미지는 어차피 다른곳에서 수정하니까,
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하시려면 아래 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
//
//        account.generateEmailCheckToken();
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//        mailMessage.setTo(account.getEmail());
//        mailMessage.setSubject("스터디올래, 로그인 링크");
//        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() +
//                "&email=" + account.getEmail());
//        javaMailSender.send(mailMessage);
    }


    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
        // add할떄 주의할 점. 어카운트가 Detached객체임. 먼저 DB에서 읽어와야함.

    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags(); // orElseThrow도 반환타입이 Account라서 getTags. 예외 나면 예외 던지고 아니면 겟태그 반환하라는거인듯.

    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag)); //set의 remove
        //지금은 Tag 객체가 사라진게 아니라, account가 들고있는 Set의 요소 하나하나를 제거할 뿐
        //해당 객체의 참조는 살아있게 된다.
        //태그를 어떻게 구현하느냐에 따라 원본 객체까지 제거하거나, 아니면 태그의 생성/삭제는
        //admin에서 따로 관리하고 그걸 각 유져가 가져다 쓰게만 할 수도 있음.

    }


    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    //컨트롤러에 있던거 리팩토링.
    public Account getAccount(String nickname) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if(nickname == null){
            throw new IllegalArgumentException(nickname + " 에 해당하는 사용자가 없습니다.");
        }
        return byNickname;
    }
}
