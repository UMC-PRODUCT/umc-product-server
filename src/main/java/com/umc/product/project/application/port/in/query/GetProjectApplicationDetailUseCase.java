package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;

/**
 * 지원서 단건 상세 조회 UseCase.
 * <p>
 * 메타(지원자/매칭 차수/상태/시각) + 폼 구조(마스킹된 섹션) + 제출된 답변 + 첨부 파일 메타까지 한 번에 조립한다.
 * <p>
 * 권한 검사는 별도 PR 에서 도입되며, 본 UseCase 는 status 와 무관하게(임시저장 DRAFT 포함) 응답을 그대로 반환한다.
 */
public interface GetProjectApplicationDetailUseCase {

    /**
     * 지원서 단건 상세를 조회한다. application 미존재 또는 정합성 위반(application 의 form.project.id ≠ projectId) 시
     * PROJECT_APPLICATION_NOT_FOUND 예외를 던진다.
     */
    ProjectApplicationDetailInfo getDetail(GetProjectApplicationDetailQuery query);
}
