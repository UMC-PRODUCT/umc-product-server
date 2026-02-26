package com.umc.product.member.application.service;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService implements GetMemberUseCase {

    private final LoadMemberPort loadMemberPort;

    private final GetSchoolUseCase getSchoolUseCase;
    private final GetFileUseCase getFileUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public MemberInfo getMemberInfoById(Long memberId) {
        // 회원
        Member member = getOrThrowMember(memberId);

        // 학교명 채워넣기
        String schoolName = getSchoolUseCase.getSchoolDetail(member.getSchoolId()).schoolName();

        // 프로필 이미지 링크 채워넣기
        String profileImageId = member.getProfileImageId();
        String profileImageLink =
            profileImageId == null
                ? null
                // profileImageId가 존재하는 경우 접근 가능한 링크를 반환하도록 함
                : getFileUseCase.getById(profileImageId).fileLink();

        // 역할 채워넣기
        List<ChallengerRoleInfo> roles = getChallengerRoleUseCase.getRoles(memberId);

        return MemberInfo.from(member, schoolName, profileImageLink, roles);
    }

    @Override
    public MemberProfileInfo getMemberProfileById(Long memberId) {
        return MemberProfileInfo.from(getOrThrowMember(memberId).getProfile());
    }

    @Override
    public Map<Long, MemberInfo> getProfiles(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Member> members = loadMemberPort.findByIdIn(memberIds);

        Map<Long, String> schoolNameCache = new HashMap<>();
        Map<String, String> profileLinkCache = new HashMap<>();
        Map<Long, MemberInfo> results = new HashMap<>(members.size());

        for (Member member : members) {
            String schoolName = null;
            Long schoolId = member.getSchoolId();
            if (schoolId != null) {
                schoolName = schoolNameCache.computeIfAbsent(schoolId, id -> {
                    SchoolDetailInfo schoolDetailInfo = getSchoolUseCase.getSchoolDetail(id);
                    return schoolDetailInfo != null ? schoolDetailInfo.schoolName() : null;
                });
            }

            String profileImageLink = null;
            String profileImageId = member.getProfileImageId();
            if (profileImageId != null) {
                profileImageLink = profileLinkCache.computeIfAbsent(profileImageId, id -> {
                    FileInfo fileInfo = getFileUseCase.getById(id);
                    return fileInfo.fileLink();
                });
            }

            results.put(member.getId(), MemberInfo.from(member, schoolName, profileImageLink, null));
        }

        return results;
    }

    @Override
    public boolean existsById(Long memberId) {
        return loadMemberPort.existsById(memberId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return loadMemberPort.existsByEmail(email);
    }

    // === Private Methods ===

    private Member getOrThrowMember(Long memberId) {
        return loadMemberPort.findById(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
