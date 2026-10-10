package com.knuaf.oneday.entity;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * 컴퓨터학부 전공 코드.
 * users.major 컬럼에는 기존과 같이 한글 전공명(displayName)이 저장됩니다.
 */
public enum MajorCode {
    ADVANCED_COMPUTING("심화컴퓨팅전공", "심화컴퓨터공학", Set.of("심화컴퓨터공학", "심화컴퓨터공학전공")),
    PLATFORM_SW("플랫폼SW융합전공", "플랫폼SW융합전공", Set.of()),
    GLOBAL_SW("글로벌SW융합전공", "글로벌SW융합전공", Set.of("글로벌소프트웨어융합전공")),
    AI_COMPUTING("인공지능컴퓨팅전공", "인공지능컴퓨팅전공", Set.of());

    private final String displayName;
    private final String trackName;     // 졸업판정 결과에 표시되는 이름
    private final Set<String> aliases;  // 과거에 저장된 다른 표기

    MajorCode(String displayName, String trackName, Set<String> aliases) {
        this.displayName = displayName;
        this.trackName = trackName;
        this.aliases = aliases;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTrackName() {
        return trackName;
    }

    /** ABEEK 공학인증(전공기반·공학전공) 학점 체계를 따르는 전공인지 */
    public boolean isAbeek() {
        return this == ADVANCED_COMPUTING || this == PLATFORM_SW;
    }

    /** 졸업 규칙 테이블에서 사용할 전공 (플랫폼SW는 심화컴퓨팅 규칙을 따름) */
    public MajorCode ruleMajor() {
        return this == PLATFORM_SW ? ADVANCED_COMPUTING : this;
    }

    public static Optional<MajorCode> find(String value) {
        if (value == null) return Optional.empty();
        String name = value.trim();
        return Arrays.stream(values())
                .filter(code -> code.displayName.equals(name) || code.aliases.contains(name))
                .findFirst();
    }

    public static MajorCode from(String value) {
        return find(value).orElseThrow(() ->
                new IllegalArgumentException("전공 정보가 올바르지 않습니다. (현재값: " + (value == null ? "" : value.trim()) + ")"));
    }
}
