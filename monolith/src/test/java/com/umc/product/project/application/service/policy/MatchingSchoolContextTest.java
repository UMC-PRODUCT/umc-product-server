package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MatchingSchoolContextTest {

    @Test
    void 후보의_학교_정보가_없으면_같은_학교_인원수를_0으로_반환한다() {
        // given
        MatchingSchoolContext sut = new MatchingSchoolContext(Map.of(1L, 10L));
        sut.addMember(100L, 1L);

        // when
        int count = sut.countSameSchoolMembers(100L, 2L);

        // then
        assertThat(count).isZero();
    }

    @Test
    void 학교_정보가_없는_구성원은_제외하고_나머지_구성원을_집계한다() {
        // given
        MatchingSchoolContext sut = new MatchingSchoolContext(Map.of(1L, 10L, 3L, 10L));

        // when
        sut.addMember(100L, 2L);
        sut.addMember(100L, 1L);

        // then
        assertThat(sut.countSameSchoolMembers(100L, 3L)).isEqualTo(1);
    }

    @Test
    void 같은_회원은_프로젝트마다_한_번만_집계한다() {
        // given
        MatchingSchoolContext sut = new MatchingSchoolContext(Map.of(1L, 10L, 2L, 10L));

        // when
        sut.addMember(100L, 1L);
        sut.addMember(100L, 1L);
        sut.addMember(200L, 1L);

        // then
        assertThat(sut.countSameSchoolMembers(100L, 2L)).isEqualTo(1);
        assertThat(sut.countSameSchoolMembers(200L, 2L)).isEqualTo(1);
    }
}
