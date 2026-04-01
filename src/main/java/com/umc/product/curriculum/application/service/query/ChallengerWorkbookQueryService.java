package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionDetailInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadReviewPort;
import com.umc.product.curriculum.application.port.out.LoadSubmissionPort;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.Review;
import com.umc.product.curriculum.domain.Submission;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerWorkbookQueryService implements GetChallengerWorkbookUseCase {

    private final LoadWorkbookSubmissionPort loadWorkbookSubmissionPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadSubmissionPort loadSubmissionPort;
    private final LoadReviewPort loadReviewPort;
    private final GetStudyGroupUseCase getStudyGroupUseCase;
    private final GetFileUseCase getFileUseCase;


    // <-------------------- 운영진이 챌린저 조회 --------------------->
    @Override
    public List<WorkbookSubmissionInfo> getSubmissions(GetWorkbookSubmissionsQuery query) {
        List<WorkbookSubmissionInfo> submissions = loadWorkbookSubmissionPort.findSubmissions(query);

        Map<String, String> urlMap = submissions.stream()
                .map(WorkbookSubmissionInfo::profileImageUrl)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> getFileUseCase.getById(id).fileLink()));

        return submissions.stream()
                .map(s -> new WorkbookSubmissionInfo(
                        s.challengerWorkbookId(),
                        s.challengerId(),
                        s.memberName(),
                        s.challengerName(),
                        urlMap.getOrDefault(s.profileImageUrl(), s.profileImageUrl()),
                        s.schoolName(),
                        s.part(),
                        s.workbookTitle(),
                        s.status()
                ))
                .toList();
    }

    @Override
    public WorkbookSubmissionDetailInfo getSubmissionDetail(Long challengerWorkbookId) {
        ChallengerWorkbook challengerWorkbook = loadChallengerWorkbookPort.findById(challengerWorkbookId);
        Optional<Submission> submissionOpt = loadSubmissionPort.findByChallengerWorkbookId(challengerWorkbookId);

        String content = submissionOpt.map(Submission::getContent).orElse(null);
        List<Review> reviews = submissionOpt
                .map(s -> loadReviewPort.findAllBySubmissionId(s.getId()))
                .orElse(List.of());

        return WorkbookSubmissionDetailInfo.builder()
                .challengerWorkbookId(challengerWorkbook.getId())
                .status(challengerWorkbook.getStatus())
                .content(content)
                .reviews(reviews.stream()
                        .map(r -> WorkbookSubmissionDetailInfo.ReviewInfo.builder()
                                .reviewId(r.getId())
                                .reviewerChallengerId(r.getReviewerChallengerId())
                                .feedback(r.getFeedback())
                                .bestReason(r.getBestReason())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public List<StudyGroupFilterInfo> getStudyGroupsForFilter(Long schoolId, ChallengerPart part) {
        StudyGroupListQuery query = new StudyGroupListQuery(schoolId, part, null, 100);
        return getStudyGroupUseCase.getStudyGroups(query).stream()
                .map(info -> new StudyGroupFilterInfo(info.groupId(), info.name()))
                .toList();
    }
}
