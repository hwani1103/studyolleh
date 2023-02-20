package com.studyolleh.study;

import com.studyolleh.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);
    // EntityGraphType.LOAD = 지정한건 EAGER로 가져오고, 나머지는 기본 전략에 따름.
    // EntityGraphType.FECTH = 지정한건 EAGER, 나머지는 LAZY
    @EntityGraph(value = "Study.withAll", type= EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

}
