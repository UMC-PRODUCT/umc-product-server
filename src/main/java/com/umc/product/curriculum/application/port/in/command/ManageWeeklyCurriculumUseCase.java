package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditWeeklyCurriculumCommand;

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
