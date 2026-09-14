package com.umc.product.member.application.service;

import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRegistrationValidator {

    private final GetTermUseCase getTermUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetFileUseCase getFileUseCase;

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
