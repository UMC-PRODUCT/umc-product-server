package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditCurriculumCommand;

/**
 * 커리큘럼 및 주차별 커리큘럼 관리 UseCase
 * <p>
 * 중앙운영사무국 교육국 소속 파트장 이상 권한 필요
 */
public interface ManageCurriculumUseCase {

    // ==== Curriculum CUD ====

    /**
     * 커리큘럼 생성
     * <p>
     * 동일한 기수에 동일한 파트에 대한 커리큘럼은 존재할 수 없습니다.
     *
     * @param command 생성 커맨드 (기수 ID, 파트, 제목)
     * @return 생성된 커리큘럼 ID
     */
    Long create(CreateCurriculumCommand command);

    /**
     * 커리큘럼 수정
     * <p>
     * 기수, 파트는 수정이 불가능하며, 커리큘럼 제목만 수정 가능합니다.
     *
     * @param command 수정 커맨드 (커리큘럼 ID, 변경할 제목)
     */
    void edit(EditCurriculumCommand command);

    /**
     * 커리큘럼 삭제 (중앙운영사무국 총괄단 이상 권한 필요)
     * <p>
     * 내부에 주차별 커리큘럼이 존재하는 경우 삭제 불가능합니다.
     *
     * @param curriculumId 삭제 대상 커리큘럼 ID
     */
    void delete(Long curriculumId);

}
