package com.umc.product.curriculum.application.service.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.curriculum.application.port.in.query.GetChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.ChallengerWorkbookInfo;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadMissionFeedbackPort;
import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.MissionFeedback;
import com.umc.product.curriculum.domain.MissionSubmission;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerWorkbookQueryService implements GetChallengerWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadMissionSubmissionPort loadMissionSubmissionPort;
    private final LoadMissionFeedbackPort loadMissionFeedbackPort;

    @Override
    public ChallengerWorkbookInfo getById(Long challengerWorkbookId) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(challengerWorkbookId);
        List<MissionSubmission> submissions =
            loadMissionSubmissionPort.findByChallengerWorkbookId(challengerWorkbookId);
        List<Long> submissionIds = submissions.stream()
            .map(MissionSubmission::getId)
            .toList();
        Map<Long, List<MissionFeedback>> feedbacksBySubmissionId = submissionIds.isEmpty()
            ? Map.of()
            : loadMissionFeedbackPort.findByMissionSubmissionIdIn(submissionIds).stream()
                .collect(Collectors.groupingBy(feedback -> feedback.getMissionSubmission().getId()));

        return ChallengerWorkbookInfoAssembler.toInfo(workbook, submissions, feedbacksBySubmissionId, false);
    }
}
