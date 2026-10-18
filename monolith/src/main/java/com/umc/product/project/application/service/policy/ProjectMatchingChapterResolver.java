package com.umc.product.project.application.service.policy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingChapterTransferPort;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 매칭에서 챌린저가 어느 지부 소속으로 취급되는지 계산합니다.
 * <p>
 * 규칙은 하나입니다. 기수 내 활성 매칭 지부 이동이 있으면 이동지부, 없으면 회원 학교 기준 지부(원지부)입니다.
 * 지원 검증, 지원 가능 플래그, 매칭 통계 모집단, 랜덤매칭 후보는 모두 이 클래스를 통해 지부를 판단해야 하며,
 * project 도메인 안에서 학교 기준 지부를 직접 계산해 매칭 판단에 쓰지 않습니다.
 */
@Component
@RequiredArgsConstructor
public class ProjectMatchingChapterResolver {

    private final LoadProjectMatchingChapterTransferPort loadTransferPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    /**
     * 회원 학교 기준 지부(원지부)를 조회합니다. 학교가 없거나 해당 기수에 학교가 지부에 배정되지 않았으면 비어 있습니다.
     */
    public Optional<ChapterInfo> findOriginChapter(Long memberId, Long gisuId) {
        Long schoolId = getMemberUseCase.getById(memberId).schoolId();
        if (schoolId == null) {
            return Optional.empty();
        }
        return getChapterUseCase.findByGisuAndSchool(gisuId, schoolId);
    }

    /**
     * 기수 내 활성 매칭 지부 이동을 조회합니다.
     */
    public Optional<ProjectMatchingChapterTransfer> findActiveTransfer(Long memberId, Long gisuId) {
        return loadTransferPort.findActiveByGisuIdAndMemberId(gisuId, memberId);
    }

    /**
     * 매칭 지부 ID 를 계산합니다. 활성 이동이 있으면 이동지부, 없으면 원지부입니다.
     */
    public Optional<Long> findMatchingChapterId(Long memberId, Long gisuId) {
        Optional<ProjectMatchingChapterTransfer> transfer = findActiveTransfer(memberId, gisuId);
        if (transfer.isPresent()) {
            return Optional.of(transfer.get().getToChapterId());
        }
        return findOriginChapter(memberId, gisuId).map(ChapterInfo::id);
    }

    /**
     * 회원이 해당 기수에 주어진 지부 소속으로 매칭되는지 확인합니다.
     */
    public boolean isInMatchingChapter(Long memberId, Long gisuId, Long chapterId) {
        return findMatchingChapterId(memberId, gisuId)
            .map(matchingChapterId -> Objects.equals(matchingChapterId, chapterId))
            .orElse(false);
    }

    /**
     * 학교 기준으로 구한 지부 인원 목록을 매칭 지부 기준으로 보정할 때 필요한 이동 정보를 계산합니다.
     * <p>
     * 학교 기준 목록에서 {@code outboundMemberIds} 를 빼고 {@code inboundChallengers} 를 더하면 매칭 지부 인원이 됩니다.
     * 들어온 챌린저의 상태(ACTIVE 여부 등) 필터는 호출자가 학교 기준 목록과 같은 기준으로 적용합니다.
     */
    public ChapterTransferDelta deltaOf(Long chapterId) {
        List<ProjectMatchingChapterTransfer> transfers = loadTransferPort.listActiveByChapterId(chapterId);
        if (transfers.isEmpty()) {
            return ChapterTransferDelta.empty();
        }

        Set<Long> outboundMemberIds = transfers.stream()
            .filter(transfer -> Objects.equals(transfer.getFromChapterId(), chapterId))
            .map(ProjectMatchingChapterTransfer::getMemberId)
            .collect(Collectors.toSet());

        List<ProjectMatchingChapterTransfer> inbound = transfers.stream()
            .filter(transfer -> Objects.equals(transfer.getToChapterId(), chapterId))
            .toList();
        if (inbound.isEmpty()) {
            return new ChapterTransferDelta(outboundMemberIds, List.of());
        }

        // 지부는 하나의 기수에 속하므로 들어온 이동의 기수는 모두 같다.
        Long gisuId = inbound.getFirst().getGisuId();
        Set<Long> inboundMemberIds = inbound.stream()
            .map(ProjectMatchingChapterTransfer::getMemberId)
            .collect(Collectors.toCollection(HashSet::new));
        Map<Long, ChallengerInfo> challengerByMember =
            getChallengerUseCase.listByMemberIdsAndGisuId(inboundMemberIds, gisuId);

        return new ChapterTransferDelta(outboundMemberIds, List.copyOf(challengerByMember.values()));
    }

    /**
     * 지부 인원 보정 정보.
     *
     * @param outboundMemberIds  이 지부 학교 소속이지만 다른 지부로 이동한 회원 ID
     * @param inboundChallengers 다른 지부 학교 소속이지만 이 지부로 이동해 온 챌린저
     */
    public record ChapterTransferDelta(Set<Long> outboundMemberIds, List<ChallengerInfo> inboundChallengers) {

        public static ChapterTransferDelta empty() {
            return new ChapterTransferDelta(Set.of(), List.of());
        }

        /**
         * 학교 기준 지부 인원 목록을 매칭 지부 기준으로 보정합니다.
         */
        public List<ChallengerInfo> apply(List<ChallengerInfo> schoolBasedChallengers) {
            if (outboundMemberIds.isEmpty() && inboundChallengers.isEmpty()) {
                return schoolBasedChallengers;
            }
            Set<Long> seen = new HashSet<>();
            List<ChallengerInfo> staying = schoolBasedChallengers.stream()
                .filter(challenger -> !outboundMemberIds.contains(challenger.memberId()))
                .filter(challenger -> seen.add(challenger.memberId()))
                .toList();
            List<ChallengerInfo> joined = inboundChallengers.stream()
                .filter(challenger -> seen.add(challenger.memberId()))
                .toList();
            return Stream.concat(staying.stream(), joined.stream()).toList();
        }
    }
}
