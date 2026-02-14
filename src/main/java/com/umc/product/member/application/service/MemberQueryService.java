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
    public MemberInfo getById(Long memberId) {
        // 회원
        Member member = loadMemberPort.findById(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));

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
    public MemberProfileInfo getProfile(Long memberId) {
        MemberInfo memberInfo = getById(memberId);

        String schoolName = null;
        if (memberInfo.schoolId() != null) {
            SchoolDetailInfo schoolDetailInfo = getSchoolUseCase.getSchoolDetail(memberInfo.schoolId());

            if (schoolDetailInfo != null) {
                schoolName = schoolDetailInfo.schoolName();
            }
        }

        String profileImageLink = null;
        if (memberInfo.profileImageId() != null) {
            FileInfo fileInfo = getFileUseCase.getById(memberInfo.profileImageId());
            profileImageLink = fileInfo.fileLink();
        }

        List<ChallengerRoleInfo> roles = getChallengerRoleUseCase.getRoles(memberId);

        return MemberProfileInfo.from(memberInfo, schoolName, profileImageLink, roles);
    }

    @Override
    public Map<Long, MemberProfileInfo> getProfiles(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Member> members = loadMemberPort.findByIdIn(memberIds);

        Map<Long, String> schoolNameCache = new HashMap<>();
        Map<String, String> profileLinkCache = new HashMap<>();
        Map<Long, MemberProfileInfo> results = new HashMap<>(members.size());

        for (Member member : members) {
            MemberInfo memberInfo = MemberInfo.from(member);

            String schoolName = null;
            Long schoolId = memberInfo.schoolId();
            if (schoolId != null) {
                schoolName = schoolNameCache.computeIfAbsent(schoolId, id -> {
                    SchoolDetailInfo schoolDetailInfo = getSchoolUseCase.getSchoolDetail(id);
                    return schoolDetailInfo != null ? schoolDetailInfo.schoolName() : null;
                });
            }

            String profileImageLink = null;
            String profileImageId = memberInfo.profileImageId();
            if (profileImageId != null) {
                profileImageLink = profileLinkCache.computeIfAbsent(profileImageId, id -> {
                    FileInfo fileInfo = getFileUseCase.getById(id);
                    return fileInfo.fileLink();
                });
            }

            results.put(memberInfo.id(), MemberProfileInfo.from(memberInfo, schoolName, profileImageLink));
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
}
