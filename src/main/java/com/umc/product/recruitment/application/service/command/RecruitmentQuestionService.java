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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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

        // 1. 검증 : Recruitment 존재
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // 2. 검증 : partKey가 COMMON이 아니면 모집 중인 파트인지
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

        // 1. 검증 : InterviewQuestionSheet 존재
        InterviewQuestionSheet question = loadInterviewQuestionSheetPort.findById(command.questionId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_NOT_FOUND));

        // 2. 검증 : 해당 Recruitment의 질문인지
        if (!question.getRecruitment().getId().equals(command.recruitmentId())) {
            throw new RecruitmentDomainException(
                RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 3. 질문 content 수정
        question.changeContent(command.questionText());

        return new UpdateInterviewSheetQuestionResult(
            question.getId(),
            question.getContent()
        );
    }

    @Override
    public void delete(DeleteInterviewSheetQuestionCommand command) {
        // 1. 검증 : InterviewQuestionSheet 존재
        InterviewQuestionSheet question = loadInterviewQuestionSheetPort.findById(command.questionId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_NOT_FOUND));

        // 2. 검증 : 해당 Recruitment의 질문인지
        if (!question.getRecruitment().getId().equals(command.recruitmentId())) {
            throw new RecruitmentDomainException(
                RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 3. 질문 삭제
        saveInterviewQuestionSheetPort.deleteById(question.getId());
    }

    @Override
    public ReorderInterviewSheetQuestionResult reorder(ReorderInterviewSheetQuestionCommand command) {
        // 1. 검증 : Recruitment 존재
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // 2. 해당 파트 기존 질문들 조회
        List<InterviewQuestionSheet> existingQuestions = loadInterviewQuestionSheetPort
            .findByRecruitmentAndPartKey(recruitment, command.partKey());

        // 3. 검증 : 기존 질문 ID 목록과 요청된 ID 목록 일치하는지
        Set<Long> existingIds = existingQuestions.stream()
            .map(InterviewQuestionSheet::getId)
            .collect(Collectors.toSet());

        List<Long> orderedIds = command.orderedQuestionIds();
        Set<Long> requestedIds = new HashSet<>(command.orderedQuestionIds());

        // 3-1. ID 중복 검증
        if (requestedIds.size() != command.orderedQuestionIds().size()) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_DUPLICATE);
        }

        // 3-2. ID 일치 검증
        if (!existingIds.equals(requestedIds)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_SHEET_QUESTION_MISMATCH);
        }

        // 4. id -> InterviewQuestionSheet 엔티티 매핑
        Map<Long, InterviewQuestionSheet> questionMap = existingQuestions.stream()
            .collect(Collectors.toMap(InterviewQuestionSheet::getId, Function.identity()));

        // 5. 순서 업데이트
        for (int i = 0; i < orderedIds.size(); i++) {
            Long questionId = orderedIds.get(i);
            InterviewQuestionSheet question = questionMap.get(questionId);
            question.changeOrderNo(i + 1);
        }

        return new ReorderInterviewSheetQuestionResult(
            command.partKey(),
            orderedIds
        );
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
