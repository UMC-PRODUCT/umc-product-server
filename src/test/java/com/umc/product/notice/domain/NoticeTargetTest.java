package com.umc.product.notice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoticeTargetTest {

    @Nested
    @DisplayName("isGlobalNotice")
    class IsGlobalNoticeTest {

        @Test
        void 모든_대상이_null이면_전체_공지이다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isTrue();
        }

        @Test
        void 파트가_빈_리스트이고_나머지_null이면_전체_공지이다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetChallengerPart(List.of())
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isTrue();
        }

        @Test
        void 기수가_지정되면_전체_공지가_아니다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetGisuId(7L)
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isFalse();
        }

        @Test
        void 지부가_지정되면_전체_공지가_아니다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetChapterId(3L)
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isFalse();
        }

        @Test
        void 학교가_지정되면_전체_공지가_아니다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetSchoolId(5L)
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isFalse();
        }

        @Test
        void 파트가_지정되면_전체_공지가_아니다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetChallengerPart(List.of(ChallengerPart.SPRINGBOOT))
                    .build();

            // when & then
            assertThat(target.isGlobalNotice()).isFalse();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        void 대상_정보를_업데이트한다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                    .noticeId(1L)
                    .targetGisuId(7L)
                    .build();

            // when
            target.update(8L, 3L, 5L, List.of(ChallengerPart.WEB));

            // then
            assertThat(target.getTargetGisuId()).isEqualTo(8L);
            assertThat(target.getTargetChapterId()).isEqualTo(3L);
            assertThat(target.getTargetSchoolId()).isEqualTo(5L);
            assertThat(target.getTargetChallengerPart()).containsExactly(ChallengerPart.WEB);
        }
    }
}
