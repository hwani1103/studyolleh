package com.studyolleh.settings;

import com.studyolleh.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor // 이 객체는 회원 정보 수정 객체인데, 프로필 수정 생성자가 있기 때문에
// 기본 생성자가 생성되지 않음. 그러면 스프링 부트가 ModelAttribute에 담아주려 할 때 널포인터 익셉션이 발생한다.
// 항상 기본 생성자로 만들어서 주입해주기 때문. 그래서 기본생성자를 선언해주거나
// NoArgsConstructor를 달아주자.
public class Profile {

    @Length(max = 35)
    private String bio;

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String occupation;

    @Length(max = 50)
    private String location;

    private String profileImage;

    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }
}
