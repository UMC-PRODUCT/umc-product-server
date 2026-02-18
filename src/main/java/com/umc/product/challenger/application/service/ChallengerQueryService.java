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
    public ChallengerInfo getChallengerPublicInfo(Long challengerId) {
        Challenger challenger = loadChallengerPort.getById(challengerId);

        return ChallengerInfo.from(challenger, getChallengerPointUseCase.getListByChallengerId(challengerId));
    }

    @Override
    public ChallengerInfo getByMemberIdAndGisuId(Long memberId, Long gisuId) {
        Challenger challenger = loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));

        return getChallengerInfoFromChallenger(challenger);
    }

    @Override
    public ChallengerInfo getActiveByMemberIdAndGisuId(Long memberId, Long gisuId) {
        Challenger challenger = loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));

        challenger.validateChallengerStatus();

        return getChallengerInfoFromChallenger(challenger);
    }

    @Override
    public List<ChallengerInfo> getMemberChallengerList(Long memberId) {
        List<Challenger> challengers = loadChallengerPort.findByMemberId(memberId);
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
    public Map<Long, ChallengerInfo> getChallengerPublicInfoByIds(Set<Long> challengerIds) {
        if (challengerIds == null || challengerIds.isEmpty()) {
            return Map.of();
        }
        return loadChallengerPort.findByIdIn(challengerIds).stream()
            .collect(Collectors.toMap(
                Challenger::getId,
                this::getChallengerInfoFromChallenger
            ));
    }

    @Override
    public List<ChallengerInfo> getByGisuId(Long gisuId) {
        return loadChallengerPort.findByGisuId(gisuId).stream()
            .map(this::getChallengerInfoFromChallenger)
            .toList();
    }

    @Override
    public List<ChallengerInfo> getByGisuIds(List<Long> gisuIds) {
        return loadChallengerPort.findByGisuIdIn(gisuIds).stream()
            .map(this::getChallengerInfoFromChallenger)
            .toList();
    }

    private ChallengerInfo getChallengerInfoFromChallenger(Challenger challenger) {
        List<ChallengerPointInfo> challengerPointInfos =
            getChallengerPointUseCase.getListByChallengerId(challenger.getId());

        return ChallengerInfo.from(challenger, challengerPointInfos);
    }
}
