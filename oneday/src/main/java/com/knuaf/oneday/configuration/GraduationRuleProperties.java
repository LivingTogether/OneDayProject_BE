package com.knuaf.oneday.configuration;

import com.knuaf.oneday.entity.MajorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** graduation-rules.yml 바인딩 */
@Getter
@Setter
@ConfigurationProperties(prefix = "graduation")
public class GraduationRuleProperties {

    private List<GeneralCredit> generalCredits = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private List<GswTrack> gswTracks = new ArrayList<>();
    private Map<String, List<Course>> courseGroups = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class GeneralCredit {
        private int fromYear;
        private int toYear;
        private int min;
        private int max;
    }

    @Getter
    @Setter
    public static class Rule {
        private MajorCode major;
        private int fromYear;
        private int toYear;
        private int totalCredits;
        private int majorCredits;
        private int engScore;
        private int abeekGeneral;
        private int abeekBaseMajor;
        private int abeekEnginMajor;
        private int abeekTotal;
        private List<String> courseGroups = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class GswTrack {
        private String track;
        private int engScore;
        private int overseasCredits;
        private int entreLecture;
    }

    @Getter
    @Setter
    public static class Course {
        private String code;
        private String name;
    }
}
