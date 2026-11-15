package com.umc.product.member.application.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberGraduationQueryService {

    private final GetChallengerUseCase getChallengerUseCase;

    public boolean hasGraduatedHistory(Set<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return false;
        }
        return getChallengerUseCase.getAllBasicByMemberIds(memberIds).values().stream()
            .flatMap(java.util.Collection::stream)
            .anyMatch(info -> info.challengerStatus() == ChallengerStatus.GRADUATED);
    }
}
