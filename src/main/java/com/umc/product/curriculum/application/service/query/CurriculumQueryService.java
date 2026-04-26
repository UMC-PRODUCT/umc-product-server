package com.umc.product.curriculum.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumOverviewInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumOverviewInfo.WeeklyCurriculumOverviewInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProjection;
import com.umc.product.curriculum.application.port.in.query.dto.MyCurriculumInfo;
import com.umc.product.curriculum.application.port.in.query.dto.MyCurriculumInfo.*;
import com.umc.product.curriculum.application.port.out.*;
import com.umc.product.curriculum.domain.*;
import com.umc.product.curriculum.domain.enums.FeedbackResult;
import com.umc.product.curriculum.domain.enums.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumQueryService implements GetCurriculumUseCase {

    private final GetChallengerUseCase getChallengerUseCase;
    private final LoadCurriculumPort loadCurriculumPort;
    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final LoadOriginalWorkbookMissionPort loadOriginalWorkbookMissionPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadMissionSubmissionPort loadMissionSubmissionPort;
    private final LoadMissionFeedbackPort loadMissionFeedbackPort;

    /**
     * 기수(gisuId)와 파트(part)에 해당하는 커리큘럼의 주차 목록을 반환합니다.
     * <p>
     * weekNo가 null이면 전체 주차를, 지정하면 해당 주차만 반환합니다.
     */
    @Override
    public CurriculumOverviewInfo getCurriculumOverview(Long gisuId, ChallengerPart part, Long weekNo) {
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, part);
        List<WeeklyCurriculum> weeklyCurriculums = loadWeeklyCurriculumPort.findByCurriculumId(projection.id(), weekNo);

        List<WeeklyCurriculumOverviewInfo> weeks = weeklyCurriculums.stream()
            .map(WeeklyCurriculumOverviewInfo::of)
            .toList();

        return CurriculumOverviewInfo.of(projection, weeks);
    }

    /**
     * 특정 멤버의 커리큘럼 진행 현황을 반환합니다.
     * <p>
     * 커리큘럼 내 전체 주차에 대해 배포된 워크북, 미션, 제출물, 피드백을 포함합니다.
     * 챌린저 파트를 기반으로 커리큘럼을 식별합니다.
     */
    @Override
    public MyCurriculumInfo getMyProgress(Long memberId, Long gisuId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, challengerInfo.part());

        List<WeeklyCurriculum> weeklyCurriculums = loadWeeklyCurriculumPort.findByCurriculumId(projection.id(), null);

        List<MyWeeklyCurriculumInfo> weeks = weeklyCurriculums.stream()
            .map(wc -> buildWeeklyCurriculumInfo(wc, memberId))
            .toList();

        return MyCurriculumInfo.of(projection, weeks);
    }

    /**
     * 주차별 커리큘럼에서 배포된 원본 워크북 목록을 조회하고 멤버 관점의 진행 정보를 구성합니다.
     */
    private MyWeeklyCurriculumInfo buildWeeklyCurriculumInfo(WeeklyCurriculum wc, Long memberId) {
        List<OriginalWorkbook> releasedWorkbooks = loadOriginalWorkbookPort.findReleasedByWeeklyCurriculumId(wc.getId());

        List<MyOriginalWorkbookInfo> workbookItems = releasedWorkbooks.stream()
            .map(wb -> buildMyOriginalWorkbookInfo(wb, memberId))
            .toList();

        return MyWeeklyCurriculumInfo.of(wc, workbookItems);
    }

    /**
     * 원본 워크북에 대한 멤버의 진행 정보를 구성합니다.
     * <p>
     * 미션 제출 여부와 챌린저 워크북 배포 여부를 함께 반영합니다.
     */
    private MyOriginalWorkbookInfo buildMyOriginalWorkbookInfo(OriginalWorkbook wb, Long memberId) {
        List<OriginalWorkbookMission> missions = loadOriginalWorkbookMissionPort.findByOriginalWorkbookId(wb.getId());

        Optional<ChallengerWorkbook> challengerWorkbook = loadChallengerWorkbookPort
            .findByMemberIdAndOriginalWorkbookId(memberId, wb.getId());

        Map<Long, MissionSubmissionInfo> submissionByMissionId = buildSubmissionMap(challengerWorkbook);

        List<MyOriginalWorkbookMissionInfo> missionInfos = missions.stream()
            .map(m -> MyOriginalWorkbookMissionInfo.of(m, submissionByMissionId.get(m.getId())))
            .toList();

        return MyOriginalWorkbookInfo.of(wb, missionInfos, challengerWorkbook.map(ChallengerWorkbook::getId));
    }

    /**
     * 챌린저 워크북에 속한 제출물과 피드백을 일괄 조회해 미션 ID 기준 Map으로 반환합니다.
     * <p>
     * 제출물별로 피드백을 개별 조회하지 않고 한 번에 불러온 뒤 그룹핑하여 N+1 문제를 방지합니다.
     * 챌린저 워크북이 없거나 제출물이 없으면 빈 Map을 반환합니다.
     */
    private Map<Long, MissionSubmissionInfo> buildSubmissionMap(Optional<ChallengerWorkbook> challengerWorkbook) {
        if (challengerWorkbook.isEmpty()) {
            return Map.of();
        }

        List<MissionSubmission> submissions = loadMissionSubmissionPort
            .findByChallengerWorkbookId(challengerWorkbook.get().getId());

        if (submissions.isEmpty()) {
            return Map.of();
        }

        List<Long> submissionIds = submissions.stream().map(MissionSubmission::getId).toList();
        List<MissionFeedback> allFeedbacks = loadMissionFeedbackPort.findByMissionSubmissionIdIn(submissionIds);

        Map<Long, List<MissionFeedback>> feedbacksBySubmissionId = allFeedbacks.stream()
            .collect(Collectors.groupingBy(f -> f.getMissionSubmission().getId()));

        return submissions.stream().collect(Collectors.toMap(
            s -> s.getOriginalWorkbookMission().getId(),
            s -> toSubmissionInfo(s, feedbacksBySubmissionId.getOrDefault(s.getId(), List.of()))
        ));
    }

    /**
     * 제출물 엔티티와 해당 피드백 목록으로 MissionSubmissionInfo를 생성합니다.
     */
    private MissionSubmissionInfo toSubmissionInfo(MissionSubmission s, List<MissionFeedback> feedbacks) {
        List<MissionFeedbackInfo> feedbackInfos = feedbacks.stream()
            .map(MissionFeedbackInfo::of)
            .toList();

        return MissionSubmissionInfo.of(s, resolveSubmissionStatus(feedbacks), feedbackInfos);
    }

    /**
     * 피드백 목록으로 최종 제출 상태를 결정합니다.
     * <p>
     * 피드백이 없으면 PENDING, PASS 피드백이 하나라도 있으면 PASS, 모두 FAIL이면 FAIL을 반환합니다.
     */
    private SubmissionStatus resolveSubmissionStatus(List<MissionFeedback> feedbacks) {
        if (feedbacks.isEmpty()) {
            return SubmissionStatus.PENDING;
        }
        return feedbacks.stream()
            .map(MissionFeedback::getFeedbackResult)
            .anyMatch(r -> r == FeedbackResult.PASS)
            ? SubmissionStatus.PASS
            : SubmissionStatus.FAIL;
    }
}