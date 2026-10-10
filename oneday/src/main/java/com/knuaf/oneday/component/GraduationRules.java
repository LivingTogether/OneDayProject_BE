package com.knuaf.oneday.component;

import com.knuaf.oneday.configuration.GraduationRuleProperties;
import com.knuaf.oneday.configuration.GraduationRuleProperties.Course;
import com.knuaf.oneday.configuration.GraduationRuleProperties.GeneralCredit;
import com.knuaf.oneday.configuration.GraduationRuleProperties.GswTrack;
import com.knuaf.oneday.configuration.GraduationRuleProperties.Rule;
import com.knuaf.oneday.configuration.YamlPropertySourceFactory;
import com.knuaf.oneday.dto.GraduationCriteria;
import com.knuaf.oneday.entity.MajorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 졸업 규칙 테이블(graduation-rules.yml) 조회.
 * 간단 조회와 상세 조회가 같은 기준을 쓰도록 모든 졸업 기준은 여기서만 꺼냅니다.
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(GraduationRuleProperties.class)
@PropertySource(value = "classpath:graduation-rules.yml", factory = YamlPropertySourceFactory.class, encoding = "UTF-8")
public class GraduationRules {

    private final GraduationRuleProperties properties;

    public GraduationCriteria criteria(MajorCode major, int entryYear) {
        MajorCode ruleMajor = major.ruleMajor();
        Rule rule = properties.getRules().stream()
                .filter(r -> r.getMajor() == ruleMajor && inRange(entryYear, r.getFromYear(), r.getToYear()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("졸업 규칙이 없습니다: " + major + ", " + entryYear));
        GeneralCredit general = generalCredit(entryYear);

        return GraduationCriteria.builder()
                .totalCredits(rule.getTotalCredits())
                .majorCredits(rule.getMajorCredits())
                .generalMin(general.getMin())
                .generalMax(general.getMax())
                .engScore(rule.getEngScore())
                .abeekGeneral(rule.getAbeekGeneral())
                .abeekBaseMajor(rule.getAbeekBaseMajor())
                .abeekEnginMajor(rule.getAbeekEnginMajor())
                .abeekTotal(rule.getAbeekTotal())
                .requiredCourses(requiredCourses(rule.getCourseGroups()))
                .build();
    }

    /** 글로벌SW융합전공 세부 트랙 기준 (영어·해외대학 인정학점·창업교과목) */
    public GswTrack gswTrack(String track) {
        return properties.getGswTracks().stream()
                .filter(t -> t.getTrack().equals(track))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("글로벌SW 트랙 규칙이 없습니다: " + track));
    }

    private GeneralCredit generalCredit(int entryYear) {
        return properties.getGeneralCredits().stream()
                .filter(g -> inRange(entryYear, g.getFromYear(), g.getToYear()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("교양 학점 규칙이 없습니다: " + entryYear));
    }

    private Map<String, String> requiredCourses(List<String> groups) {
        Map<String, String> courses = new LinkedHashMap<>();
        for (String group : groups) {
            List<Course> list = properties.getCourseGroups().get(group);
            if (list == null) throw new IllegalStateException("필수 과목 묶음이 없습니다: " + group);
            list.forEach(c -> courses.put(c.getCode(), c.getName()));
        }
        return courses;
    }

    private static boolean inRange(int year, int from, int to) {
        return from <= year && year <= to;
    }
}
