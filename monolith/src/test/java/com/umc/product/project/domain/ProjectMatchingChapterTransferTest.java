package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectMatchingChapterTransferTest {

    private static final Long GISU_ID = 11L;
    private static final Long MEMBER_ID = 1L;
    private static final Long SEOUL = 3L;
    private static final Long GYEONGGI = 5L;
    private static final Long ADMIN_ID = 99L;

    @Nested
    class create {

        @Test
        void 원지부와_이동지부가_다르면_활성_이동이_생성된다() {
            ProjectMatchingChapterTransfer transfer =
                ProjectMatchingChapterTransfer.create(GISU_ID, MEMBER_ID, SEOUL, GYEONGGI, "디자이너 인원 조정", ADMIN_ID);

            assertThat(transfer.isActive()).isTrue();
            assertThat(transfer.getFromChapterId()).isEqualTo(SEOUL);
            assertThat(transfer.getToChapterId()).isEqualTo(GYEONGGI);
            assertThat(transfer.getCreatedByMemberId()).isEqualTo(ADMIN_ID);
        }

        @Test
        void 원지부와_이동지부가_같으면_CHAPTER_TRANSFER_SAME_CHAPTER() {
            assertThatThrownBy(() ->
                ProjectMatchingChapterTransfer.create(GISU_ID, MEMBER_ID, SEOUL, SEOUL, null, ADMIN_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.CHAPTER_TRANSFER_SAME_CHAPTER);
        }
    }

    @Nested
    class release {

        @Test
        void 해제하면_비활성이_되고_해제자와_사유가_기록된다() {
            ProjectMatchingChapterTransfer transfer =
                ProjectMatchingChapterTransfer.create(GISU_ID, MEMBER_ID, SEOUL, GYEONGGI, null, ADMIN_ID);

            transfer.release(ADMIN_ID, "등록 실수");

            assertThat(transfer.isActive()).isFalse();
            assertThat(transfer.getReleasedAt()).isNotNull();
            assertThat(transfer.getReleasedByMemberId()).isEqualTo(ADMIN_ID);
            assertThat(transfer.getReleaseReason()).isEqualTo("등록 실수");
        }

        @Test
        void 이미_해제된_이동을_다시_해제하면_CHAPTER_TRANSFER_ALREADY_RELEASED() {
            ProjectMatchingChapterTransfer transfer =
                ProjectMatchingChapterTransfer.create(GISU_ID, MEMBER_ID, SEOUL, GYEONGGI, null, ADMIN_ID);
            transfer.release(ADMIN_ID, null);

            assertThatThrownBy(() -> transfer.release(ADMIN_ID, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.CHAPTER_TRANSFER_ALREADY_RELEASED);
        }
    }
}
