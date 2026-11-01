package com.umc.product.project.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.ProjectMatchingRound;

public interface LoadProjectMatchingRoundPort {

    ProjectMatchingRound getById(Long id);

    /**
     * 같은 차수의 선발·발표가 동시에 처리되지 않도록 차수 행을 잠금 후 조회합니다.
     */
    ProjectMatchingRound getByIdForUpdate(Long id);

    /**
     * 발표 시각이 설정되어 있고, 아직 발표 작업이 끝나지 않은 차수를 조회합니다.
     */
    List<ProjectMatchingRound> listAllNotPublished();

    Optional<ProjectMatchingRound> findById(Long id);

    List<ProjectMatchingRound> listByIds(List<Long> ids);

    List<ProjectMatchingRound> batchGetByIds(List<Long> ids);

    List<ProjectMatchingRound> listByChapterId(Long chapterId);

    /** 프로젝트 삭제와 차수별 선발·발표를 직렬화하도록 ID 순으로 잠금 조회한다. */
    List<ProjectMatchingRound> listByChapterIdForUpdate(Long chapterId);

    List<ProjectMatchingRound> listAll();

    List<ProjectMatchingRound> listOpenAt(Long chapterId, Instant time);

    List<ProjectMatchingRound> listOverlapping(Long chapterId, Instant startsAt, Instant decisionDeadline);

    List<ProjectMatchingRound> listOverlappingExceptId(
        Long id, Long chapterId, Instant startsAt, Instant decisionDeadline
    );

    /**
     * 자동 선발이 아직 실행되지 않은 매칭 차수 전체를 조회합니다 (deadline 경과 여부 무관).
     * <p>
     * 동적 스케줄링 컴포넌트가 부팅 시 미처리 round 를 모두 등록하기 위해 사용합니다.
     */
    List<ProjectMatchingRound> listAllNotAutoDecided();
}
