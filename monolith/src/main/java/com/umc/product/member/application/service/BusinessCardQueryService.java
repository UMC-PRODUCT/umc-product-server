package com.umc.product.member.application.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.in.query.GetBusinessCardUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.BusinessCardEditInfo;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.out.LoadBusinessCardActivityPort;
import com.umc.product.member.application.port.out.LoadMemberOAuthProviderPort;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.dto.BusinessCardActivity;
import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 회원 프로필을 바탕으로 명함을 구성합니다.
 * <p>
 * 명함은 별도로 저장하지 않는 회원 프로필의 projection이므로, 프로필·링크·공개 설정을 수정하면 바로 반영됩니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessCardQueryService implements GetBusinessCardUseCase {

    private static final Comparator<BusinessCardActivity> LATEST_GENERATION_FIRST = Comparator.comparing(
        BusinessCardActivity::generation,
        Comparator.nullsLast(Comparator.reverseOrder())
    );

    private final GetMemberUseCase getMemberUseCase;
    private final LoadMemberPort loadMemberPort;
    private final LoadBusinessCardActivityPort loadBusinessCardActivityPort;
    private final LoadMemberOAuthProviderPort loadMemberOAuthProviderPort;

    @Override
    public BusinessCardInfo getByMemberId(Long memberId) {
        MemberInfo member = getMemberUseCase.getById(memberId);
        MemberProfile profile = getProfile(memberId);

        List<BusinessCardInfo.Link> links = allLinks(profile).stream()
            .filter(BusinessCardInfo.Link::isPublic)
            .filter(link -> new LinkTypeAndLink(link.type(), link.link()).isOpenable())
            .toList();

        return new BusinessCardInfo(
            member.id(),
            member.name(),
            member.nickname(),
            member.schoolName(),
            member.profileImageLink(),
            latestActivity(memberId),
            publicContact(profile),
            links
        );
    }

    @Override
    public BusinessCardEditInfo getEditByMemberId(Long memberId) {
        MemberInfo member = getMemberUseCase.getById(memberId);
        MemberProfile profile = getProfile(memberId);

        return new BusinessCardEditInfo(
            member.id(),
            member.name(),
            member.schoolName(),
            member.profileImageLink(),
            latestActivity(memberId),
            loadMemberOAuthProviderPort.listByMemberId(memberId),
            contact(profile),
            allLinks(profile)
        );
    }

    private MemberProfile getProfile(Long memberId) {
        return loadMemberPort.findById(memberId)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND))
            .getProfile();
    }

    /**
     * 활동 이력 중 가장 최신 기수의 기수·파트를 표시합니다. 활동 이력이 없으면 null입니다.
     */
    private BusinessCardInfo.Activity latestActivity(Long memberId) {
        return loadBusinessCardActivityPort.listByMemberId(memberId).stream()
            .min(LATEST_GENERATION_FIRST)
            .map(latest -> new BusinessCardInfo.Activity(latest.gisuId(), latest.generation(), latest.part()))
            .orElse(null);
    }

    private BusinessCardInfo.Contact contact(MemberProfile profile) {
        if (profile == null) {
            return new BusinessCardInfo.Contact(null, true);
        }
        return new BusinessCardInfo.Contact(profile.getContactEmail(), profile.isContactEmailPublic());
    }

    private BusinessCardInfo.Contact publicContact(MemberProfile profile) {
        BusinessCardInfo.Contact contact = contact(profile);
        if (!contact.isPublic() || !hasText(contact.email())) {
            return null;
        }
        return contact;
    }

    /**
     * 링크 타입마다 하나씩 enum 순서로 반환합니다. 입력하지 않은 링크는 link가 null입니다.
     */
    private List<BusinessCardInfo.Link> allLinks(MemberProfile profile) {
        return Arrays.stream(MemberProfileLinkType.values())
            .map(type -> profile == null
                ? new BusinessCardInfo.Link(type, null, true)
                : new BusinessCardInfo.Link(type, textOrNull(profile.getLink(type)), profile.isLinkPublic(type)))
            .toList();
    }

    private static String textOrNull(String value) {
        return hasText(value) ? value : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
