package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerQueryService implements GetChallengerUseCase {

    private final LoadChallengerPort loadChallengerPort;
    private final GetChallengerPointUseCase getChallengerPointUseCase;

    @Override
    public ChallengerInfo getById(Long challengerId) {
        return getChallengerInfoFromChallenger(loadChallengerPort.getById(challengerId));
    }

    @Override
    public ChallengerInfo findByIdOrNull(Long challengerId) {
        Challenger challenger = loadChallengerPort.findById(challengerId).orElse(null);

        return challenger != null
            ? getChallengerInfoFromChallenger(challenger)
            : null;
    }

    @Override
    public ChallengerInfo getByMemberIdAndGisuId(Long memberId, Long gisuId) {
        Challenger challenger = loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));

        return getChallengerInfoFromChallenger(challenger);
    }

    @Override
    public Optional<ChallengerInfo> findByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .map(this::getChallengerInfoFromChallenger);
    }

    @Override
    public ChallengerInfo getActiveByMemberIdAndGisuId(Long memberId, Long gisuId) {
        Challenger challenger = loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));

        challenger.validateChallengerStatus();

        return getChallengerInfoFromChallenger(challenger);
    }

    @Override
    public List<ChallengerInfo> getAllByMemberId(Long memberId) {
        List<Challenger> challengers = loadChallengerPort.getAllByMemberId(memberId);
        return challengers.stream()
            .map(this::getChallengerInfoFromChallenger)
            .toList();
    }

    @Override
    public ChallengerInfoWithStatus getLatestActiveChallengerByMemberId(Long memberId) {
        Challenger challenger = loadChallengerPort.findTopByMemberIdOrderByCreatedAtDesc(memberId);
        if (challenger.getStatus() == ChallengerStatus.WITHDRAWN
            || challenger.getStatus() == ChallengerStatus.EXPELLED) {
            throw new ChallengerDomainException(ChallengerErrorCode.NOT_ALLOWED_AUTHOR);
        }
        return ChallengerInfoWithStatus.from(challenger);
    }

    @Override
    public Map<Long, ChallengerInfo> getAllByIdsAsMap(Set<Long> challengerIds) {
        if (challengerIds == null || challengerIds.isEmpty()) {
            return Map.of();
        }
        return loadChallengerPort.getAllByIds(challengerIds).stream()
            .collect(Collectors.toMap(
                Challenger::getId,
                this::getChallengerInfoFromChallenger
            ));
    }

    @Override
    public List<ChallengerInfo> getAllByIds(Set<Long> challengerIds) {
        if (challengerIds == null || challengerIds.isEmpty()) {
            return List.of();
        }
        return loadChallengerPort.getAllByIds(challengerIds).stream()
            .map(this::getChallengerInfoFromChallenger).toList();
    }

    @Override
    public List<ChallengerInfo> getAllByGisuId(Long gisuId) {
        return toChallengerInfoListBatch(loadChallengerPort.getAllByGisuId(gisuId));
    }

    @Override
    public List<ChallengerInfo> getAllByGisuIdWithoutChallengerPoints(Long gisuId) {
        return loadChallengerPort.getAllByGisuId(gisuId).stream()
            .map(c -> ChallengerInfo.from(c, List.of()))
            .toList();
    }

    @Override
    public List<ChallengerInfo> getAllLatestGisuPerMemberWithoutChallengerPoints() {
        return loadChallengerPort.findLatestPerMember().stream()
            .map(ChallengerInfo::from)
            .toList();
    }

    private ChallengerInfo getChallengerInfoFromChallenger(Challenger challenger) {
        List<ChallengerPointInfo> challengerPointInfos =
            getChallengerPointUseCase.getListByChallengerId(challenger.getId());

        return ChallengerInfo.from(challenger, challengerPointInfos);
    }

    /**
     * 챌린저 목록의 포인트를 IN 쿼리 1번으로 일괄 조회해 ChallengerInfo 리스트로 변환합니다.
     */
    private List<ChallengerInfo> toChallengerInfoListBatch(List<Challenger> challengers) {
        if (challengers.isEmpty()) {
            return List.of();
        }

        Set<Long> ids = challengers.stream()
            .map(Challenger::getId)
            .collect(Collectors.toSet());

        Map<Long, List<ChallengerPointInfo>> pointsMap =
            getChallengerPointUseCase.getMapByChallengerIds(ids);

        return challengers.stream()
            .map(c -> ChallengerInfo.from(c, pointsMap.getOrDefault(c.getId(), List.of())))
            .toList();
    }
}
