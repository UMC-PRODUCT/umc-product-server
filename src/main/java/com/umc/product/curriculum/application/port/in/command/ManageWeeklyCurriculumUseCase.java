package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditWeeklyCurriculumCommand;
import java.util.List;

public interface ManageWeeklyCurriculumUseCase {

    // ==== WeeklyCurriculum CUD ====

    /**
     * 주차별 커리큘럼 생성
     * <p>
     * 각 커리큘럼에 대해 주차별 커리큘럼은 최대 2개(MAIN, EXTRA) 생성 가능합니다.
     *
     * @param command 생성 커맨드 (커리큘럼 ID, 주차, 부록 여부, 제목, 시작/종료 일시)
     * @return 생성된 주차별 커리큘럼 ID
     */
    Long create(CreateWeeklyCurriculumCommand command);

    /**
     * 주차별 커리큘럼 일괄 생성 (atomic batch).
     * <p>
     * 동일 트랜잭션 안에서 N 건을 순차 처리해 동일 curriculumId 의 검증 SELECT 가 1 차 캐시 활용으로
     * 1 회로 묶이고, 트랜잭션 commit 횟수도 N → 1 회로 감소한다. 도메인 검증은 단건 {@link #create}
     * 와 동일하게 매 command 별로 수행되며, 한 건 실패 시 전체 롤백된다.
     *
     * @return 생성된 주차별 커리큘럼 ID 목록 (입력 순서 보존)
     */
    List<Long> createBulk(List<CreateWeeklyCurriculumCommand> commands);

    /**
     * 주차별 커리큘럼 수정
     * <p>
     * 배포된 워크북이 하나라도 존재하면 시작/종료일 수정이 불가능합니다.
     *
     * @param command 수정 커맨드 (주차별 커리큘럼 ID, 변경할 필드들)
     */
    void edit(EditWeeklyCurriculumCommand command);

    /**
     * 주차별 커리큘럼 삭제
     * <p>
     * 포함된 원본 워크북이 하나라도 존재하면 삭제 불가능합니다.
     *
     * @param weeklyCurriculumId 삭제 대상 주차별 커리큘럼 ID
     */
    void delete(Long weeklyCurriculumId);
}
