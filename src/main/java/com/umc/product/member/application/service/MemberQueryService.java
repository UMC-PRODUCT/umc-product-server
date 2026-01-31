package com.umc.product.member.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
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

    @Override
    public MemberInfo getById(Long memberId) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    public MemberInfo getByEmail(String email) {
        Member member = loadMemberPort.findByEmail(email)
                .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    public MemberProfileInfo getProfile(Long memberId) {
        MemberInfo memberInfo = getById(memberId);

        String schoolName = null;
        if (memberInfo.schoolId() != null) {
            SchoolInfo schoolInfo = getSchoolUseCase.getSchoolDetail(memberInfo.schoolId());

            // 학교 정보 null이 아닌 경우에만 학교 이름 할당 - 미구현상태라 null check 추가
            if (schoolInfo != null) {
                schoolName = schoolInfo.schoolName();
            }
        }

        String profileImageLink = null;
        if (memberInfo.profileImageId() != null) {
            FileInfo fileInfo = getFileUseCase.getById(memberInfo.profileImageId());
            profileImageLink = fileInfo.fileLink();
        }

        return MemberProfileInfo.from(memberInfo, schoolName, profileImageLink);
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
