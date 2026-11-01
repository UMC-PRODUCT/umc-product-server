package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.project.domain.ProjectMatchingChapterTransfer;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(ProjectMatchingChapterTransferPersistenceAdapter.class)
class ProjectMatchingChapterTransferPersistenceAdapterTest {

    private static final Long GISU_ID = 11L;
    private static final Long SEOUL = 3L;
    private static final Long GYEONGGI = 5L;
    private static final Long BUSAN = 7L;
    private static final Long ADMIN_ID = 99L;

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectMatchingChapterTransferPersistenceAdapter sut;

    @Test
    @DisplayName("기수·회원의 활성 이동만 조회하고 해제된 이동은 무시한다")
    void 활성_이동만_조회한다() {
        ProjectMatchingChapterTransfer released = transfer(1L, SEOUL, BUSAN);
        released.release(ADMIN_ID, "등록 실수");
        em.persist(released);
        ProjectMatchingChapterTransfer active = em.persist(transfer(1L, SEOUL, GYEONGGI));
        em.flush();
        em.clear();

        assertThat(sut.findActiveByGisuIdAndMemberId(GISU_ID, 1L))
            .get()
            .extracting(ProjectMatchingChapterTransfer::getId)
            .isEqualTo(active.getId());
        assertThat(sut.findActiveByGisuIdAndMemberId(GISU_ID + 1, 1L)).isEmpty();
    }

    @Test
    @DisplayName("지부 기준 조회는 그 지부로 들어오거나 나간 활성 이동을 모두 반환한다")
    void 지부로_들어오고_나간_활성_이동을_조회한다() {
        em.persist(transfer(1L, SEOUL, GYEONGGI));
        em.persist(transfer(2L, GYEONGGI, SEOUL));
        em.persist(transfer(3L, BUSAN, GYEONGGI));
        ProjectMatchingChapterTransfer released = transfer(4L, SEOUL, BUSAN);
        released.release(ADMIN_ID, null);
        em.persist(released);
        em.flush();
        em.clear();

        List<ProjectMatchingChapterTransfer> result = sut.listActiveByChapterId(SEOUL);

        assertThat(result).extracting(ProjectMatchingChapterTransfer::getMemberId)
            .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("같은 기수·회원에 활성 이동이 두 개면 부분 유니크 인덱스가 막는다")
    void 활성_이동_중복은_DB가_막는다() {
        em.persist(transfer(1L, SEOUL, GYEONGGI));
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(transfer(1L, SEOUL, BUSAN));
            em.flush();
        }).rootCause().hasMessageContaining("uk_project_matching_chapter_transfer_active");
    }

    @Test
    @DisplayName("해제된 이동이 있어도 같은 기수·회원에 새 활성 이동을 등록할 수 있다")
    void 해제_후_재등록은_허용된다() {
        ProjectMatchingChapterTransfer released = transfer(1L, SEOUL, GYEONGGI);
        released.release(ADMIN_ID, null);
        em.persist(released);
        em.persist(transfer(1L, SEOUL, BUSAN));
        em.flush();
        em.clear();

        assertThat(sut.findActiveByGisuIdAndMemberId(GISU_ID, 1L))
            .get()
            .extracting(ProjectMatchingChapterTransfer::getToChapterId)
            .isEqualTo(BUSAN);
    }

    private static ProjectMatchingChapterTransfer transfer(Long memberId, Long from, Long to) {
        return ProjectMatchingChapterTransfer.create(GISU_ID, memberId, from, to, null, ADMIN_ID);
    }
}
