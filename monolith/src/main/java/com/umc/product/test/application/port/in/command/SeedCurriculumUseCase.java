package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedCurriculumCommand;
import com.umc.product.test.application.port.in.command.dto.SeedCurriculumResult;

/**
 * 활성 기수(또는 지정 기수)에 대해 Curriculum / WeeklyCurriculum / OriginalWorkbook /
 * OriginalWorkbookMission 골격을 시딩한다. ADR-017 참조.
 */
public interface SeedCurriculumUseCase {

    SeedCurriculumResult seed(SeedCurriculumCommand command);
}
