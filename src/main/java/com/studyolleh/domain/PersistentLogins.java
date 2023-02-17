package com.studyolleh.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

//Jdbc토큰 리포지토리가 사용하는 테이블. 이 있어야 함.
//Jpa에서 인메모리 DB를 쓸때는 엔티티 정보를 보고 테이블을 만들어줌.
//그렇기 때문에 Jdbc토큰리포지토리에 맵핑이 되는 엔티티를 만들어줘야함.
//무슨말인지 모르겠기 때문에 일단 다 받아적고 스프링 시큐리티를 공부할 때 정복하도록 하겠다
//이번 프로젝트에서 가장 이해가 부족한부분이 바로 여기랑 remeberme토큰.
@Table(name = "persistent_logins")
@Entity
@Getter @Setter
public class PersistentLogins {

    @Id
    @Column(length = 64)
    private String series;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false, length = 64)
    private LocalDateTime lastUsed;

}
