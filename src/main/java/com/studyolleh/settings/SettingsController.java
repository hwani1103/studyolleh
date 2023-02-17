package com.studyolleh.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.CurrentUser;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Tag;
import com.studyolleh.domain.Zone;
import com.studyolleh.settings.form.*;
import com.studyolleh.settings.validator.NicknameValidator;
import com.studyolleh.settings.validator.PasswordFormValidator;
import com.studyolleh.tag.TagRepository;
import com.studyolleh.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {
    private final ModelMapper modelMapper;
    private final ZoneRepository zoneRepository;
    private final AccountService accountService;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    // List. 자바 객체 List타입을 JSON 문자열로 변환하려면 ObjectMapper
    // 기본적으로 스프링부트에는 faster xml이 제공하는 objectmapper가 기본적으로
    // 빈으로 등록이 되어 있음. 이 오브젝트 맵퍼가 지금까지 사용했던
    // RequestBody, ResponseBody
    // JSON Parsing 할때 핵심적인 인스턴스가 ObjectMapper이다.

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";

    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/settings/password";

    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_URL = "/settings/notifications";

    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTINGS_ACCOUNT_URL = "/" + SETTINGS_ACCOUNT_VIEW_NAME;

    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAGS_URL = "/" + SETTINGS_TAGS_VIEW_NAME;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")// nicknameForm을 처리할떄 nicknameValidator를 사용해달라!!
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }


    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account,
                                @Valid @ModelAttribute Profile profile,
                                Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }
        //리다이렉트시키고 한번 쓰고 사라질 데이타. 모델에 자동으로 들어가고 한번 쓰고 없어지는기능. FLash
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        accountService.updateProfile(account, profile);
        return "redirect:/settings/profile";
    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + SETTINGS_PASSWORD_URL;

    }

    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Notifications(account));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
//        if (errors.hasErrors()) {
//            model.addAttribute(account);
//            return SETTINGS_NOTIFICATIONS_VIEW_NAME; // 이거 무슨 의도인지 모르겠네. 일단 삭제해도 잘 동작됨.
//        }                 // 일단 어떤 에러가 담길 상황인지 모르겠고, 그 때 account를 모델에 담아서 다시 보낸다?
        // 아.

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
    }


    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }



    //=============================
    // "/settings/tags"
    //지금 여기의 update는 SQL관점에서의 update가 아니라
    //그냥 최신화하는 update인듯.
    //생성된 태그를 변경했을떄 그걸 변경 적용하는 update 그런게 아니다.
    //그냥 현재 회원 account의 태그를 가져와서 보여주고, 화이트리스트를 보여주고 그런용도.
    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        //태그가 문자열로 바꾸고, Collectors의 toList로 결론. List<String>으로 반환.

        //tag를 전부 가져온다음, List<String>으로 변환하고 나서
        //ObjectMapper를 사용해서 JSON타입으로 변환해서 뷰에 보낸다!
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
                                                    // string json타입으로 변환.
        return SETTINGS_TAGS_VIEW_NAME;
    }

//    @PostMapping("/settings/tags/add")
//    public String addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
//        String title = tagForm.getTagTitle();
//        Tag tag = tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
//                .title(tagForm.getTagTitle())
//                .build()));
//        return SETTINGS_TAGS_VIEW_NAME;
//    } // 위아래 같은코드지만 위는 옵셔널. TagRepository에도 옵셔널로 받는거랑 Tag로 받는거 두개 다 있음. 일단 옵셔널 아닌걸로 적용하자.
        // 이후에 ResponseEntity로 반환하고 코드 조금 수정됐음. 그 전까지 같았음.




    // ajax post요청을 받는 메서드라서 반환타입도 ResponseEntity.
    // 매개변수도 RequestBody, 애너테이션도 ResponseBody
    // 응답자체가 리스폰스바디가 되어야 함. 응답 본문. 이때 우리가 넘겨주는 타입은
    // 리스폰스 엔티티가 되어야한다. 리스폰스엔티티.ok.build()를 넘겨주면 됨.

    // 지금 태그 add랑 update정확히 어떤 차이인지 모르겠네? 수정은 등록된 태그를 말그대로 수정하는 기능이네
    // add는 add이고 update는 사실상 조회인데, Ajax요청이니까 update로 표시한 듯 .

    @PostMapping(SETTINGS_TAGS_URL + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            return ResponseEntity.badRequest().build(); // 없는 태그를 삭제하려는 요청은 배드 리퀘스트 빌드 리턴.
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }


    //=======================거주지역.





    @GetMapping("/settings/zones")
    public String updateZonesForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "settings/zones";
    }

    @PostMapping("/settings/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) { // 태그는 태그폼의 Title으로 읽어와서 없으면 만들지만 존은 없으면 에러발생.
            //존을 admin이 관리한다는 뜻.
            return ResponseEntity.badRequest().build();
        }
        // 존은 이미 있는 Zone의 참조를 해당 회원에게 전달.
        // 태그는 이미 있는 Tag는 참조를 전달하고, 없는 태그는 만든다.

        accountService.addZone(account, zone); // 널이 아니고 있으면, 로그인회원한테 add.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        //삭제는 태그랑 존이랑 똑같음. 없는 존을 삭제하려면 배드리퀘스트.
        //삭제는 해당 회원의 존에서만 삭제.
        //엄밀히 Zone의 구현이 조금 더 논리적인듯
        //Zone을 회원에서 삭제할때는 해당 회원의 Set에서만 remove하면됨. DB에서는 어차피 재사용되니까.
        //Tag의 경우, DB까지 삭제해줘야 한다. DB에서는 재사용이 될 수도 있지만 안될수도 있음.
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }











}
// 바인딩받을때 에러? 밸리데이션 위반 에러?
// 모델은 자동으로 그 form을 채웠던 데이터는 자동으로 들어가고
// 그데이터와 더불어 에러에 대한 정보도 모델에 자동으로 들어간다.
// account만 넣어주면된다.