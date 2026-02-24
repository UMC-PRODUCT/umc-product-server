package com.umc.product.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FcmTopicNameTest {

    @Nested
    class 개별_토픽_생성 {

        @Test
        void 기수_토픽() {
            assertThat(FcmTopicName.gisu(1L)).isEqualTo("gisu-1");
        }

        @Test
        void 기수_파트_토픽() {
            assertThat(FcmTopicName.gisuPart(1L, ChallengerPart.SPRINGBOOT))
                    .isEqualTo("gisu-1-part-SPRINGBOOT");
        }

        @Test
        void 기수_학교_토픽() {
            assertThat(FcmTopicName.gisuSchool(1L, 5L))
                    .isEqualTo("gisu-1-school-5");
        }

        @Test
        void 기수_학교_파트_토픽() {
            assertThat(FcmTopicName.gisuSchoolPart(1L, 5L, ChallengerPart.WEB))
                    .isEqualTo("gisu-1-school-5-part-WEB");
        }

        @Test
        void 기수_지부_토픽() {
            assertThat(FcmTopicName.gisuChapter(1L, 3L))
                    .isEqualTo("gisu-1-chapter-3");
        }

        @Test
        void 기수_지부_파트_토픽() {
            assertThat(FcmTopicName.gisuChapterPart(1L, 3L, ChallengerPart.ANDROID))
                    .isEqualTo("gisu-1-chapter-3-part-ANDROID");
        }
    }

    @Nested
    class allTopicsFor {

        @Test
        void 학교_지부_모두_있으면_6개_토픽_반환() {
            List<String> topics = FcmTopicName.allTopicsFor(
                    1L, ChallengerPart.SPRINGBOOT, 5L, 3L);

            assertThat(topics).containsExactly(
                    "gisu-1",
                    "gisu-1-part-SPRINGBOOT",
                    "gisu-1-school-5",
                    "gisu-1-school-5-part-SPRINGBOOT",
                    "gisu-1-chapter-3",
                    "gisu-1-chapter-3-part-SPRINGBOOT"
            );
        }

        @Test
        void 학교만_있고_지부가_없으면_4개_토픽_반환() {
            List<String> topics = FcmTopicName.allTopicsFor(
                    2L, ChallengerPart.WEB, 10L, null);

            assertThat(topics).containsExactly(
                    "gisu-2",
                    "gisu-2-part-WEB",
                    "gisu-2-school-10",
                    "gisu-2-school-10-part-WEB"
            );
        }

        @Test
        void 지부만_있고_학교가_없으면_4개_토픽_반환() {
            List<String> topics = FcmTopicName.allTopicsFor(
                    1L, ChallengerPart.IOS, null, 7L);

            assertThat(topics).containsExactly(
                    "gisu-1",
                    "gisu-1-part-IOS",
                    "gisu-1-chapter-7",
                    "gisu-1-chapter-7-part-IOS"
            );
        }

        @Test
        void 학교_지부_모두_없으면_2개_토픽_반환() {
            List<String> topics = FcmTopicName.allTopicsFor(
                    3L, ChallengerPart.DESIGN, null, null);

            assertThat(topics).containsExactly(
                    "gisu-3",
                    "gisu-3-part-DESIGN"
            );
        }
    }

    @Nested
    class resolveTopics {

        @Test
        void 지부와_파트가_있으면_지부_파트별_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(
                    1L, 3L, null, List.of(ChallengerPart.WEB, ChallengerPart.ANDROID));

            assertThat(topics).containsExactly(
                    "gisu-1-chapter-3-part-WEB",
                    "gisu-1-chapter-3-part-ANDROID"
            );
        }

        @Test
        void 지부만_있고_파트가_없으면_지부_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(1L, 3L, null, null);

            assertThat(topics).containsExactly("gisu-1-chapter-3");
        }

        @Test
        void 지부만_있고_파트가_빈_리스트면_지부_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(1L, 3L, null, List.of());

            assertThat(topics).containsExactly("gisu-1-chapter-3");
        }

        @Test
        void 학교와_파트가_있으면_학교_파트별_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(
                    1L, null, 5L, List.of(ChallengerPart.SPRINGBOOT));

            assertThat(topics).containsExactly("gisu-1-school-5-part-SPRINGBOOT");
        }

        @Test
        void 학교만_있고_파트가_없으면_학교_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(1L, null, 5L, null);

            assertThat(topics).containsExactly("gisu-1-school-5");
        }

        @Test
        void 파트만_있으면_기수_파트별_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(
                    1L, null, null, List.of(ChallengerPart.PLAN, ChallengerPart.DESIGN));

            assertThat(topics).containsExactly(
                    "gisu-1-part-PLAN",
                    "gisu-1-part-DESIGN"
            );
        }

        @Test
        void 지부_학교_파트_모두_없으면_기수_토픽_반환() {
            List<String> topics = FcmTopicName.resolveTopics(1L, null, null, null);

            assertThat(topics).containsExactly("gisu-1");
        }

        @Test
        void 지부가_학교보다_우선순위가_높다() {
            // chapterId와 schoolId 모두 있을 때 chapterId 기준으로 토픽 생성
            List<String> topics = FcmTopicName.resolveTopics(
                    1L, 3L, 5L, List.of(ChallengerPart.WEB));

            assertThat(topics).containsExactly("gisu-1-chapter-3-part-WEB");
        }
    }
}
