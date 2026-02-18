package com.umc.product.notice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.notice.domain.exception.NoticeDomainException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoticeTest {

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        void 공지사항을_생성한다() {
            // when
            Notice notice = Notice.create("제목", "내용", 1L, true);

            // then
            assertThat(notice.getTitle()).isEqualTo("제목");
            assertThat(notice.getContent()).isEqualTo("내용");
            assertThat(notice.getAuthorMemberId()).isEqualTo(1L);
            assertThat(notice.isShouldSendNotification()).isTrue();
            assertThat(notice.getNotifiedAt()).isNull();
        }

        @Test
        void 알림_미발송_공지사항을_생성한다() {
            // when
            Notice notice = Notice.create("제목", "내용", 1L, false);

            // then
            assertThat(notice.isShouldSendNotification()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateTitleOrContent")
    class UpdateTest {

        @Test
        void 제목과_내용을_수정한다() {
            // given
            Notice notice = Notice.create("원래 제목", "원래 내용", 1L, false);

            // when
            notice.updateTitleOrContent("수정된 제목", "수정된 내용");

            // then
            assertThat(notice.getTitle()).isEqualTo("수정된 제목");
            assertThat(notice.getContent()).isEqualTo("수정된 내용");
        }
    }

    @Nested
    @DisplayName("validateAuthorChallenger")
    class ValidateAuthorTest {

        @Test
        void 작성자와_일치하면_예외가_발생하지_않는다() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, false);

            // when & then (예외 없음)
            notice.validateAuthorMember(1L);
        }

        @Test
        void 작성자와_일치하지_않으면_예외가_발생한다() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, false);

            // when & then
            assertThatThrownBy(() -> notice.validateAuthorMember(999L))
                .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("isNotificationRequired")
    class NotificationRequiredTest {

        @Test
        void 알림_발송_설정이고_아직_발송하지_않았으면_true() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, true);

            // when & then
            assertThat(notice.isNotificationRequired()).isTrue();
        }

        @Test
        void 알림_발송_설정이_아니면_false() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, false);

            // when & then
            assertThat(notice.isNotificationRequired()).isFalse();
        }

        @Test
        void 이미_알림을_발송했으면_false() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, true);
            notice.markAsNotified(Instant.now());

            // when & then
            assertThat(notice.isNotificationRequired()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsNotified")
    class MarkAsNotifiedTest {

        @Test
        void 알림_발송_시각을_기록한다() {
            // given
            Notice notice = Notice.create("제목", "내용", 1L, true);
            Instant now = Instant.now();

            // when
            notice.markAsNotified(now);

            // then
            assertThat(notice.getNotifiedAt()).isEqualTo(now);
        }
    }
}
