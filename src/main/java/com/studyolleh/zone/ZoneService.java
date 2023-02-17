package com.studyolleh.zone;

import com.studyolleh.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    // ZoneService빈이 생성된 다음 바로 수행됨.
    @PostConstruct // Zone. csv파일을 읽어와서 초기화를 해야 하는데. PostConstruct로 초기화를 하겠다는것.
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0) { // 리포지토리에 등록된 zone이 없으면.
            Resource resource = new ClassPathResource("zones_kr.csv"); //Resource는 springframework 패키지. 파일경로 .
            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {      //메서드 잘 봐야될듯.
                        String[] split = line.split(","); // 컴마로 짜른다. 배열로 받는다.
                        return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                        //스트림 순환하면서. 스플릿한거를 알맞게 저장한 뒤 build.
                    }).collect(Collectors.toList());
            zoneRepository.saveAll(zoneList);
        }
    }

}
