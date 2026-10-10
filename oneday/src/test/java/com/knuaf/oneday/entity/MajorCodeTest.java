package com.knuaf.oneday.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MajorCodeTest {

    @Test
    void 저장된_전공명과_과거_표기를_전공_코드로_변환한다() {
        assertThat(MajorCode.from("심화컴퓨팅전공")).isEqualTo(MajorCode.ADVANCED_COMPUTING);
        assertThat(MajorCode.from(" 플랫폼SW융합전공 ")).isEqualTo(MajorCode.PLATFORM_SW);
        assertThat(MajorCode.from("글로벌소프트웨어융합전공")).isEqualTo(MajorCode.GLOBAL_SW);
        assertThat(MajorCode.from("인공지능컴퓨팅전공")).isEqualTo(MajorCode.AI_COMPUTING);
    }

    @Test
    void 플랫폼SW는_심화컴퓨팅_졸업_규칙을_따른다() {
        assertThat(MajorCode.PLATFORM_SW.ruleMajor()).isEqualTo(MajorCode.ADVANCED_COMPUTING);
        assertThat(MajorCode.PLATFORM_SW.isAbeek()).isTrue();
    }

    @Test
    void 알_수_없는_전공은_예외() {
        assertThat(MajorCode.find("경영학과")).isEmpty();
        assertThatThrownBy(() -> MajorCode.from("경영학과"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전공 정보가 올바르지 않습니다");
    }
}
