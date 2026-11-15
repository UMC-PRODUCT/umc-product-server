package com.umc.product.member.application.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.LoadNicknameReservationPort;
import com.umc.product.member.application.port.out.LockSchoolNicknamePort;
import com.umc.product.member.domain.MemberNamePolicy;
import com.umc.product.member.domain.MemberRejoinKey;
import com.umc.product.member.domain.NicknamePolicy;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberRegistrationValidator {

    private final GetTermUseCase getTermUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetFileUseCase getFileUseCase;
    private final LockSchoolNicknamePort lockSchoolNicknamePort;
    private final LoadMemberPort loadMemberPort;
    private final LoadNicknameReservationPort loadNicknameReservationPort;

    protected void lockSchoolsForRegistration(List<Long> schoolIds) {
        schoolIds.stream().distinct().sorted().forEach(lockSchoolNicknamePort::lockSchool);
    }

    protected void validateNicknameAvailable(Long schoolId, String nickname, String email) {
        lockSchoolNicknamePort.lockSchool(schoolId);
        String ownerKey = MemberRejoinKey.fromVerifiedEmail(email);
        var graduatedReservation = loadNicknameReservationPort.findGraduatedByOwner(ownerKey);
        graduatedReservation
            .filter(reservation -> !reservation.schoolId().equals(schoolId)
                || !reservation.nickname().equals(nickname))
            .ifPresent(reservation -> {
                throw new MemberDomainException(MemberErrorCode.GRADUATED_REJOIN_NICKNAME_MISMATCH);
            });
        if (graduatedReservation.isEmpty()) {
            NicknamePolicy.validate(nickname);
        }
        if (loadMemberPort.existsBySchoolIdAndNickname(schoolId, nickname)) {
            throw new MemberDomainException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        if (loadNicknameReservationPort.isReserved(schoolId, nickname)
            && !loadNicknameReservationPort.isReclaimableBy(schoolId, nickname, ownerKey)) {
            throw new MemberDomainException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    protected void validateName(String name) {
        MemberNamePolicy.validate(name);
    }

    /**
     * 사진 ID가 주어진 경우 해당 파일이 존재하는지 확인
     */
    protected void validateProfileImageExists(String profileImageId) {
        if (profileImageId != null) {
            getFileUseCase.throwIfNotExists(profileImageId);
        }
    }

    protected void validateSchoolExists(Long schoolId) {
        getSchoolUseCase.getSchoolDetail(schoolId);
    }

    protected void validateMandatoryTermsAgreed(List<TermConsents> termConsents) {
        Set<Long> requiredTermIds = getTermUseCase.getRequiredTermIds();

        Set<Long> agreedTermIds = termConsents.stream()
            .filter(TermConsents::isAgreed)
            .map(TermConsents::termId)
            .collect(Collectors.toSet());

        if (!agreedTermIds.containsAll(requiredTermIds)) {
            throw new TermDomainException(TermErrorCode.MANDATORY_TERMS_NOT_AGREED);
        }
    }
}
