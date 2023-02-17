package com.studyolleh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app") // app .. application.properties에 정의되어 있음.app 어쩌고
//app.host. 그게 이거.
//@ConfigurationProperties
public class AppProperties { // 스프링 부트가 제공하는 기능!?

    private String host;

}
