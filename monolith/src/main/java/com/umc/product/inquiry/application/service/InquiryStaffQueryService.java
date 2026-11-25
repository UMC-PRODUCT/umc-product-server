package com.umc.product.inquiry.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryStaffDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryAssigneeCandidatesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryLabelsUseCase;
import com.umc.product.inquiry.application.port.in.query.SearchStaffInquiriesUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryHistoryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffMemberInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffPageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffSummaryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.StaffInquiriesQuery;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryHistoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryHistory;
import com.umc.product.inquiry.domain.InquiryLabel;
import com.umc.product.inquiry.domain.enums.InquiryHistoryType;
import com.umc.product.inquiry.domain.enums.InquiryStaffTab;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStaffQueryService implements
    SearchStaffInquiriesUseCase,
    GetInquiryStaffDetailUseCase,
    ListInquiryAssigneeCandidatesUseCase,
    ListInquiryLabelsUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final LoadInquiryLabelPort loadInquiryLabelPort;
    private final LoadInquiryHistoryPort loadInquiryHistoryPort;
    private final GetInquiryDetailUseCase getInquiryDetailUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryStaffPageInfo search(StaffInquiriesQuery query) {
        accessPolicy.requireStaff(query.staffMemberId());
        LoadInquiryPort.Cursor cursor = query.cursor() == null ? null : InquiryListCursorCodec.decode(query.cursor());
        Set<Long> staffIds = staffIds();
        LoadInquiryPort.StaffSearch search = new LoadInquiryPort.StaffSearch(
            query.tab(),
            query.staffMemberId(),
            staffIds,
            query.statuses(),
            query.categoryIds(),
            query.labelIds()
        );

        List<Inquiry> rows = loadInquiryPort.searchForStaff(search, cursor, query.size() + 1);
        boolean hasNext = rows.size() > query.size();
        List<Inquiry> page = hasNext ? rows.subList(0, query.size()) : rows;

        Set<Long> inquiryIds = page.stream().map(Inquiry::getId).collect(Collectors.toSet());
        Map<Long, List<InquiryLabel>> labels = loadInquiryLabelPort.listByInquiryIds(inquiryIds);
        Map<Long, InquiryCategoryInfo> categories = categoriesOf(
            page.stream().map(Inquiry::getCategoryId).collect(Collectors.toSet())
        );
        Map<Long, String> names = names(page.stream()
            .map(inquiry -> inquiry.effectiveAssigneeId(staffIds))
            .collect(Collectors.toSet()));

        List<InquiryStaffSummaryInfo> summaries = page.stream()
            .map(inquiry -> new InquiryStaffSummaryInfo(
                inquiry.getId(),
                inquiry.getTitle(),
                categories.get(inquiry.getCategoryId()),
                inquiry.getTargetTeam(),
                inquiry.getStatus(),
                member(inquiry.effectiveAssigneeId(staffIds), names),
                labels.getOrDefault(inquiry.getId(), List.of()).stream().map(InquiryLabelInfo::from).toList(),
                inquiry.getCreatedAt(),
                inquiry.getLastMessageAt()
            ))
            .toList();
        return new InquiryStaffPageInfo(
            new InquiryStaffPageInfo.TabCounts(
                loadInquiryPort.countForStaff(search.withTab(InquiryStaffTab.ALL)),
                loadInquiryPort.countForStaff(search.withTab(InquiryStaffTab.MINE)),
                loadInquiryPort.countForStaff(search.withTab(InquiryStaffTab.UNASSIGNED))
            ),
            summaries,
            hasNext ? InquiryListCursorCodec.encode(cursorOf(page.get(page.size() - 1))) : null,
            hasNext
        );
    }

    @Override
    public InquiryStaffDetailInfo getStaffDetail(InquiryDetailQuery query) {
        accessPolicy.requireStaff(query.requesterMemberId());
        InquiryDetailInfo detail = getInquiryDetailUseCase.getDetail(query);
        Inquiry inquiry = loadInquiryPort.findById(query.inquiryId())
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
        Long assigneeId = inquiry.effectiveAssigneeId(staffIds());
        List<InquiryHistory> histories = loadInquiryHistoryPort.listByInquiryId(inquiry.getId());

        Set<Long> memberIds = new HashSet<>();
        memberIds.add(assigneeId);
        memberIds.add(inquiry.getAuthorMemberId());
        histories.forEach(history -> {
            memberIds.add(history.getActorMemberId());
            memberIds.add(history.getPreviousAssigneeMemberId());
            memberIds.add(history.getNewAssigneeMemberId());
        });
        Map<Long, String> names = names(memberIds);

        List<InquiryHistoryInfo> historyInfos = new ArrayList<>();
        historyInfos.add(new InquiryHistoryInfo(
            InquiryHistoryType.RECEIVED,
            member(inquiry.getAuthorMemberId(), names),
            null,
            null,
            null,
            null,
            inquiry.getCreatedAt()
        ));
        histories.forEach(history -> historyInfos.add(new InquiryHistoryInfo(
            history.getType(),
            member(history.getActorMemberId(), names),
            member(history.getPreviousAssigneeMemberId(), names),
            member(history.getNewAssigneeMemberId(), names),
            history.getPreviousTeam(),
            history.getNewTeam(),
            history.getCreatedAt()
        )));

        return new InquiryStaffDetailInfo(
            detail,
            member(assigneeId, names),
            loadInquiryLabelPort.listByInquiryIds(Set.of(inquiry.getId()))
                .getOrDefault(inquiry.getId(), List.of()).stream()
                .map(InquiryLabelInfo::from)
                .toList(),
            List.copyOf(historyInfos)
        );
    }

    @Override
    public List<InquiryStaffMemberInfo> listCandidates(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        Map<Long, String> names = names(staffIds());
        return names.entrySet().stream()
            .map(entry -> new InquiryStaffMemberInfo(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(InquiryStaffMemberInfo::name, Comparator.nullsLast(String::compareTo))
                .thenComparing(InquiryStaffMemberInfo::memberId))
            .toList();
    }

    @Override
    public List<InquiryLabelInfo> listLabels(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        return loadInquiryLabelPort.listAll().stream().map(InquiryLabelInfo::from).toList();
    }

    private static LoadInquiryPort.Cursor cursorOf(Inquiry last) {
        return new LoadInquiryPort.Cursor(last.getLastMessageAt(), last.getId());
    }

    private Set<Long> staffIds() {
        return Set.copyOf(listMemberSystemRoleUseCase.listUmcProductMemberIds());
    }

    private Map<Long, String> names(Set<Long> memberIds) {
        Set<Long> ids = memberIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        return ids.isEmpty() ? Map.of() : getMemberUseCase.findAllNamesByIds(ids);
    }

    private static InquiryStaffMemberInfo member(Long memberId, Map<Long, String> names) {
        return memberId == null ? null : new InquiryStaffMemberInfo(memberId, names.get(memberId));
    }

    private Map<Long, InquiryCategoryInfo> categoriesOf(Set<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return loadInquiryCategoryPort.listByIds(categoryIds).stream()
            .collect(Collectors.toMap(InquiryCategory::getId, InquiryCategoryInfo::from));
    }
}
