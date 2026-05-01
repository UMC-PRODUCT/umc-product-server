package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderFormSectionsCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormSectionCommand;

/**
 * FormSection(폼 섹션) 관리 UseCase.
 * <p>
 * 섹션은 폼 안에서 질문들을 묶는 논리적 단위. 발행된 폼의 섹션 구조 변경은
 * 응답 일관성을 해칠 수 있으므로 DRAFT 상태에서만 수정/삭제/재정렬 가능.
 */
public interface ManageFormSectionUseCase {

    /**
     * 폼에 섹션을 추가한다.
     * orderNo는 Service가 해당 폼의 마지막 섹션 뒤에 자동 부여.
     *
     * @return 생성된 FormSection ID
     */
    Long createSection(CreateFormSectionCommand command);

    /**
     * 섹션의 title/description을 수정한다.
     * 발행된 폼의 섹션은 수정 불가 — SURVEY_NOT_DRAFT 예외. TODO: 관련 로직 확정 시 수정 필요
     */
    void updateSection(UpdateFormSectionCommand command);

    /**
     * 섹션을 삭제한다. 연관 Question / QuestionOption 도 cascade 삭제.
     */
    void deleteSection(DeleteFormSectionCommand command);

    /**
     * 폼 내 섹션들의 순서를 재배치한다.
     * 입력 리스트 순서대로 orderNo가 1부터 재부여된다.
     * 리스트에 포함되지 않은 섹션이 있으면 예외.
     */
    void reorderSections(ReorderFormSectionsCommand command);
}
