package com.studyolleh.domain;

import com.studyolleh.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// 쿼리 5개나오던걸 한번에 나오게 함.
// N+1 해결방법중에 하나. 쿼리 튜닝한것.
@NamedEntityGraph(name="Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")
}) // 스터디를 조회할때, 얘네를 EAGER fetch 해라. 이거 하고 레포지토리에도 해줘야함


@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Study {

    @Id
    @GeneratedValue
    private long id;

    /**
     * Study와 Account간에 다대다 이지만 Study -> Account 단방향 관계가 2개임.
     * 매니저인 어카운트와 스터디 구성원인 어카운트.
     * 이런 관계도 있구나
     */
    @ManyToMany
    private Set<Account> managers = new HashSet<>(); // 이 스터디를 만드는 회원이 매니저. 혹은 매니저 권한을 더 줄수도 있음.

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER) // Lob은 기본값이 EAGER. 가져오도록 설정함.
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags;

    @ManyToMany
    private Set<Zone> zones;

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdateTime; // 열었다 닫았다 시간제한

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public void addManager(Account account) {
        this.managers.add(account);
    }
    public void addMemeber(Account account) {
        this.members.add(account);
    }
    public boolean isJoinable(UserAccount userAccount){ // principal. authentication.principal
        Account account = userAccount.getAccount(); // UserAccount는 Account를 들고있는 principal객체
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account)
                && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount){
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){
        return this.managers.contains(userAccount.getAccount());
    }

}
