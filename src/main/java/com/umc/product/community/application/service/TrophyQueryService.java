package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.community.application.port.in.trophy.TrophyInfo;
import com.umc.product.community.application.port.in.trophy.query.GetTrophyListUseCase;
import com.umc.product.community.application.port.in.trophy.query.TrophySearchQuery;
import com.umc.product.community.application.port.out.LoadTrophyPort;
import com.umc.product.community.domain.Trophy;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
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
public class TrophyQueryService implements GetTrophyListUseCase {

    private final LoadTrophyPort loadTrophyPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public List<TrophyInfo> getTrophies(TrophySearchQuery query) {
        List<Trophy> trophies = loadTrophyPort.findAllByQuery(query);

        // 트로피가 없으면 빈 리스트 반환
        if (trophies.isEmpty()) {
            return List.of();
        }

        // 1. 고유한 챌린저 ID 목록 추출
        Set<Long> challengerIds = trophies.stream()
                .map(trophy -> trophy.getChallengerId().id())
                .collect(Collectors.toSet());

        // 2. 챌린저 ID -> 챌린저 정보 매핑 (1 query)
        Map<Long, ChallengerInfo> challengerInfoMap = getChallengerUseCase.getChallengerPublicInfoByIds(challengerIds);

        // 3. 멤버 ID 목록 추출
        Set<Long> memberIds = challengerInfoMap.values().stream()
                .map(ChallengerInfo::memberId)
                .collect(Collectors.toSet());

        // 4. 멤버 ID -> 멤버 프로필 매핑 (1 query, 학교명 포함)
        Map<Long, MemberInfo> memberProfileMap = getMemberUseCase.getProfiles(memberIds);

        // 5. TrophyInfo로 변환
        return trophies.stream()
                .map(trophy -> {
                    Long challengerId = trophy.getChallengerId().id();
                    ChallengerInfo challengerInfo = challengerInfoMap.get(challengerId);

                    if (challengerInfo == null) {
                        return TrophyInfo.from(trophy);
                    }

                    MemberInfo memberProfile = memberProfileMap.get(challengerInfo.memberId());
                    String challengerName = memberProfile != null ? memberProfile.name() : "알 수 없음";
                    String challengerProfileImage = memberProfile != null ? memberProfile.profileImageLink() : null;
                    String school = memberProfile != null ? memberProfile.schoolName() : null;
                    String part = challengerInfo.part() != null ? challengerInfo.part().name() : null;

                    return TrophyInfo.of(trophy, challengerName, challengerProfileImage, school, part);
                })
                .toList();
    }
}
