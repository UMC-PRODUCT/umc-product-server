package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.UpdateMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateMyDocumentEvaluationCommand;
import com.umc.product.recruitment.application.port.in.query.UpdateDocumentStatusUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentDocumentEvaluationService implements UpdateMyDocumentEvaluationUseCase,
        UpdateDocumentStatusUseCase {

    @Override
    public GetMyDocumentEvaluationInfo update(UpdateMyDocumentEvaluationCommand command) {
        // todo: 평가 기간, 운영진 권한 검증 필요
        // 매번 운영진의 점수를 모두 조회해와서 평균을 내는 방식은 비효율적일 수 있으므로, 지원서 평가의 평균 점수를 지원서 엔티티에 별도로 저장해두고,
        // 평가가 등록/수정될 때마다 해당 값을 갱신하는 방식을 고려 중에 있습니다.
        return null;
    }

    @Override
    public UpdateDocumentStatusInfo update(UpdateDocumentStatusCommand command) {
        // todo: 평가 기간, 운영진 권한 검증 필요
        return null;
    }
}
