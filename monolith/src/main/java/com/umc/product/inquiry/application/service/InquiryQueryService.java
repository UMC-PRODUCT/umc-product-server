package com.umc.product.inquiry.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.chat.application.port.in.query.GetChatMessagesUseCase;
import com.umc.product.chat.application.port.in.query.dto.ChatMessageCursorResult;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessagesQuery;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryMessagesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryCategoriesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryTeamsUseCase;
import com.umc.product.inquiry.application.port.in.query.ListMyInquiriesUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryAuthorInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagePageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagesQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryPageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryTeamInfo;
import com.umc.product.inquiry.application.port.in.query.dto.MyInquiriesQuery;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryTeam;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService implements
    ListInquiryCategoriesUseCase,
    ListMyInquiriesUseCase,
    GetInquiryDetailUseCase,
    GetInquiryMessagesUseCase,
    ListInquiryTeamsUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final InquiryAccessPolicy accessPolicy;
    private final InquiryMessageAssembler messageAssembler;

    @Override
    public List<InquiryCategoryInfo> listActive() {
        return loadInquiryCategoryPort.listActive().stream()
            .map(InquiryCategoryInfo::from)
            .toList();
    }

    @Override
    public List<InquiryTeamInfo> listTeams() {
        return Stream.concat(
            Arrays.stream(InquiryTeam.values()).map(InquiryTeamInfo::from),
            Stream.of(InquiryTeamInfo.unknown())
        ).toList();
    }

    @Override
    public InquirySummaryPageInfo listMine(MyInquiriesQuery query) {
        LoadInquiryPort.Cursor cursor = query.cursor() == null ? null : InquiryListCursorCodec.decode(query.cursor());

        List<Inquiry> rows = loadInquiryPort.listByAuthor(
            query.authorMemberId(),
            query.statusGroup().statuses(),
            cursor,
            query.size() + 1
        );
        boolean hasNext = rows.size() > query.size();
        List<Inquiry> page = hasNext ? rows.subList(0, query.size()) : rows;

        Map<Long, InquiryCategoryInfo> categories = categoriesOf(
            page.stream().map(Inquiry::getCategoryId).collect(Collectors.toSet())
        );
        return new InquirySummaryPageInfo(
            page.stream()
                .map(inquiry -> InquirySummaryInfo.of(inquiry, categories.get(inquiry.getCategoryId())))
                .toList(),
            hasNext ? nextCursorOf(page.get(page.size() - 1)) : null,
            hasNext
        );
    }

    @Override
    public InquiryDetailInfo getDetail(InquiryDetailQuery query) {
        Inquiry inquiry = loadInquiry(query.inquiryId());
        InquiryParticipant requester = accessPolicy.requireParticipant(inquiry, query.requesterMemberId());
        InquiryCategoryInfo category = categoriesOf(Set.of(inquiry.getCategoryId())).get(inquiry.getCategoryId());
        InquiryAuthorInfo author = getMemberUseCase.findById(inquiry.getAuthorMemberId())
            .map(member -> new InquiryAuthorInfo(
                member.id(), member.name(), member.nickname(), member.profileImageLink()
            ))
            .orElseGet(() -> new InquiryAuthorInfo(inquiry.getAuthorMemberId(), null, null, null));

        return new InquiryDetailInfo(
            inquiry.getId(),
            inquiry.getTitle(),
            category,
            inquiry.getTargetTeam(),
            inquiry.getStatus(),
            author,
            inquiry.getCreatedAt(),
            inquiry.getClosedAt(),
            inquiry.getLastMessageAt(),
            accessPolicy.canClose(inquiry, query.requesterMemberId(), requester)
        );
    }

    @Override
    public InquiryMessagePageInfo getMessages(InquiryMessagesQuery query) {
        Inquiry inquiry = loadInquiry(query.inquiryId());
        accessPolicy.requireParticipant(inquiry, query.requesterMemberId());
        ChatMessageCursorResult page = getChatMessagesUseCase.getMessages(new GetChatMessagesQuery(
            inquiry.getChatRoomId(),
            query.requesterMemberId(),
            query.cursor(),
            query.size()
        ));
        return new InquiryMessagePageInfo(
            messageAssembler.assemble(inquiry, page.content()),
            page.nextCursor(),
            page.hasNext()
        );
    }

    private static String nextCursorOf(Inquiry last) {
        return InquiryListCursorCodec.encode(new LoadInquiryPort.Cursor(last.getLastMessageAt(), last.getId()));
    }

    private Inquiry loadInquiry(Long inquiryId) {
        return loadInquiryPort.findById(inquiryId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    private Map<Long, InquiryCategoryInfo> categoriesOf(Set<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return loadInquiryCategoryPort.listByIds(categoryIds).stream()
            .collect(Collectors.toMap(InquiryCategory::getId, InquiryCategoryInfo::from));
    }
}
