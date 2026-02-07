package com.umc.product.recruitment.application.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.CreateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.ReorderInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateInterviewSheetQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewSheetQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.ReorderInterviewSheetQuestionResult;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateInterviewSheetQuestionResult;
import com.umc.product.recruitment.application.port.out.LoadInterviewQuestionSheetPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewQuestionSheetPort;
import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitmentQuestionService implements CreateInterviewSheetQuestionUseCase,
    UpdateInterviewSheetQuestionUseCase,
    DeleteInterviewSheetQuestionUseCase,
    ReorderInterviewSheetQuestionUseCase {

    private final SaveInterviewQuestionSheetPort saveInterviewQuestionSheetPort;
    private final LoadInterviewQuestionSheetPort loadInterviewQuestionSheetPort;
    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;

    @Override
    public CreateInterviewSheetQuestionResult create(CreateInterviewSheetQuestionCommand command) {

        // 1. Recruitment 찾기
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // 2. partKey 검증 (COMMON이 아니면 모집 중인 파트인지 확인)
        validatePartKey(command.recruitmentId(), command.partKey());

        // 3. orderNo 설정 (recruitment & partKey의 InterviewQuestion 중 max + 1)
        int orderNo = loadInterviewQuestionSheetPort
            .findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(recruitment, command.partKey())
            .map(InterviewQuestionSheet::getOrderNo)
            .orElse(0) + 1;

        // 4. InterviewQuestionSheet 저장
        InterviewQuestionSheet interviewQuestionSheet = saveInterviewQuestionSheetPort.save(
            command.toEntity(recruitment, orderNo)
        );

        return new CreateInterviewSheetQuestionResult(
            interviewQuestionSheet.getId(),
            interviewQuestionSheet.getOrderNo(),
            interviewQuestionSheet.getContent()
        );
    }

    @Override
    public UpdateInterviewSheetQuestionResult update(UpdateInterviewSheetQuestionCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteInterviewSheetQuestionCommand command) {

    }

    @Override
    public ReorderInterviewSheetQuestionResult reorder(ReorderInterviewSheetQuestionCommand command) {
        return null;
    }

    private void validatePartKey(Long recruitmentId, PartKey partKey) {
        // COMMON은 항상 허용
        if (partKey == PartKey.COMMON) {
            return;
        }

        // 모집 중인 파트 목록 조회
        List<RecruitmentPart> openParts = loadRecruitmentPartPort
            .findByRecruitmentIdAndStatus(recruitmentId, RecruitmentPartStatus.OPEN);

        // partKey가 모집 중인 파트에 포함되는지 확인
        boolean isValidPart = openParts.stream()
            .map(RecruitmentPart::getPart)
            .map(ChallengerPart::name)
            .anyMatch(partName -> partName.equals(partKey.name()));

        if (!isValidPart) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_SHEET_PART_NOT_OPEN);
        }
    }
}
