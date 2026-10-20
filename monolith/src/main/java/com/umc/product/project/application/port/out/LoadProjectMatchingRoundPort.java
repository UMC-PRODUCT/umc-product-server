package com.umc.product.project.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.ProjectMatchingRound;

public interface LoadProjectMatchingRoundPort {

    ProjectMatchingRound getById(Long id);

    /**
     * 매칭 차수를 비관적 쓰기 잠금으로 조회합니다. 자동 선발·결과 공개의 중복 실행을 방지합니다.
     */
    ProjectMatchingRound getByIdForUpdate(Long id);

    /**
     * 결과 발표 시각이 설정됐으며 아직 공개되지 않은 차수를 조회합니다.
     * 서버 시작 시 결과 발표 예약을 복구하는 데 사용합니다.
     */
    List<ProjectMatchingRound> listAllNotPublished();

    Optional<ProjectMatchingRound> findById(Long id);

    List<ProjectMatchingRound> listByIds(List<Long> ids);

    List<ProjectMatchingRound> batchGetByIds(List<Long> ids);

    List<ProjectMatchingRound> listByChapterId(Long chapterId);

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
