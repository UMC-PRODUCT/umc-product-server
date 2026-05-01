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
     * 워크북·미션·제출물·피드백을 모두 IN 쿼리로 일괄 조회한 뒤 메모리에서 조립하여
     * DB 접근을 총 6회로 고정합니다 (주차→워크북→미션·챌린저워크북→제출물→피드백).
     */
    @Override
    public MyCurriculumInfo getMyProgress(Long memberId, Long gisuId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, challengerInfo.part());
        List<WeeklyCurriculum> weeklyCurriculums = loadWeeklyCurriculumPort.findByCurriculumId(projection.id(), null);

        if (weeklyCurriculums.isEmpty()) {
            return MyCurriculumInfo.of(projection, List.of());
        }

        // 전체 주차 ID로 배포된 원본 워크북 일괄 조회
        List<Long> weeklyCurriculumIds = weeklyCurriculums.stream().map(WeeklyCurriculum::getId).toList();
        List<OriginalWorkbook> allWorkbooks = loadOriginalWorkbookPort.findReleasedByWeeklyCurriculumIdIn(weeklyCurriculumIds);

        List<Long> originalWorkbookIds = allWorkbooks.stream().map(OriginalWorkbook::getId).toList();

        // 미션과 챌린저 워크북 일괄 조회
        List<OriginalWorkbookMission> allMissions = loadOriginalWorkbookMissionPort.findByOriginalWorkbookIdIn(originalWorkbookIds);
        List<ChallengerWorkbook> allChallengerWorkbooks = loadChallengerWorkbookPort.findByMemberIdAndOriginalWorkbookIdIn(memberId, originalWorkbookIds);

        // 제출물과 피드백 일괄 조회
        List<Long> challengerWorkbookIds = allChallengerWorkbooks.stream().map(ChallengerWorkbook::getId).toList();
        List<MissionSubmission> allSubmissions = loadMissionSubmissionPort.findByChallengerWorkbookIdIn(challengerWorkbookIds);
        List<Long> submissionIds = allSubmissions.stream().map(MissionSubmission::getId).toList();
        List<MissionFeedback> allFeedbacks = loadMissionFeedbackPort.findByMissionSubmissionIdIn(submissionIds);

        // 조회 결과를 ID 기준으로 그룹핑
        Map<Long, List<OriginalWorkbook>> workbooksByWcId = allWorkbooks.stream()
            .collect(Collectors.groupingBy(wb -> wb.getWeeklyCurriculum().getId()));
        Map<Long, List<OriginalWorkbookMission>> missionsByWbId = allMissions.stream()
            .collect(Collectors.groupingBy(m -> m.getOriginalWorkbook().getId()));
        Map<Long, ChallengerWorkbook> cwByWbId = allChallengerWorkbooks.stream()
            .collect(Collectors.toMap(cw -> cw.getOriginalWorkbook().getId(), cw -> cw));
        Map<Long, MissionSubmissionInfo> submissionInfoByMissionId = buildSubmissionInfoMap(allSubmissions, allFeedbacks);

        List<MyWeeklyCurriculumInfo> weeks = weeklyCurriculums.stream()
            .map(wc -> {
                List<MyOriginalWorkbookInfo> workbookInfos = workbooksByWcId.getOrDefault(wc.getId(), List.of()).stream()
                    .map(wb -> buildMyOriginalWorkbookInfo(wb, missionsByWbId, cwByWbId, submissionInfoByMissionId))
                    .toList();
                return MyWeeklyCurriculumInfo.of(wc, workbookInfos);
            })
            .toList();

        return MyCurriculumInfo.of(projection, weeks);
    }

    /**
     * 원본 워크북에 대한 멤버의 진행 정보를 구성합니다.
     * <p>
     * 사전 조회된 Map에서 O(1)으로 조회하여 추가 DB 접근 없이 조립합니다.
     */
    private MyOriginalWorkbookInfo buildMyOriginalWorkbookInfo(
        OriginalWorkbook wb,
        Map<Long, List<OriginalWorkbookMission>> missionsByWbId,
        Map<Long, ChallengerWorkbook> cwByWbId,
        Map<Long, MissionSubmissionInfo> submissionInfoByMissionId
    ) {
        List<OriginalWorkbookMission> missions = missionsByWbId.getOrDefault(wb.getId(), List.of());
        Optional<ChallengerWorkbook> challengerWorkbook = Optional.ofNullable(cwByWbId.get(wb.getId()));

        List<MyOriginalWorkbookMissionInfo> missionInfos = missions.stream()
            .map(m -> MyOriginalWorkbookMissionInfo.of(m, submissionInfoByMissionId.get(m.getId())))
            .toList();

        return MyOriginalWorkbookInfo.of(wb, missionInfos, challengerWorkbook.map(ChallengerWorkbook::getId));
    }

    /**
     * 전체 제출물과 피드백을 받아 미션 ID 기준 Map으로 변환합니다.
     * <p>
     * 피드백은 제출물 ID 기준으로 먼저 그룹핑한 뒤, 제출물 단위로 MissionSubmissionInfo를 생성합니다.
     */
    private Map<Long, MissionSubmissionInfo> buildSubmissionInfoMap(
        List<MissionSubmission> submissions,
        List<MissionFeedback> allFeedbacks
    ) {
        if (submissions.isEmpty()) {
            return Map.of();
        }

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