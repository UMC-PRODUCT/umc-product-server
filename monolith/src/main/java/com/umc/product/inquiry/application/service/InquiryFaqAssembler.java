package com.umc.product.inquiry.application.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqAudienceInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqContentInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRefInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqAudiencePort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqContent;

import lombok.RequiredArgsConstructor;

/**
 * 대표 질문 내용의 카테고리·대상·관련 안내를 한 번에 모아 읽어 응답 모양으로 만든다.
 */
@Component
@RequiredArgsConstructor
class InquiryFaqAssembler {

    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final LoadInquiryFaqAudiencePort loadInquiryFaqAudiencePort;
    private final LoadInquiryFaqPort loadInquiryFaqPort;

    Lookup lookup(Collection<InquiryFaqContent> contents) {
        List<InquiryFaqContent> present = contents.stream().filter(Objects::nonNull).toList();
        Map<Long, InquiryCategoryInfo> categories = loadInquiryCategoryPort.listByIds(
                ids(present, InquiryFaqContent::getCategoryId)).stream()
            .collect(Collectors.toMap(InquiryCategory::getId, InquiryCategoryInfo::from));
        Map<Long, InquiryFaqAudienceInfo> audiences = loadInquiryFaqAudiencePort.listByIds(
                ids(present, InquiryFaqContent::getAudienceId)).stream()
            .map(InquiryFaqAudienceInfo::from)
            .collect(Collectors.toMap(InquiryFaqAudienceInfo::audienceId, Function.identity()));
        Map<Long, InquiryFaq> related = loadInquiryFaqPort.listByIds(
                ids(present, InquiryFaqContent::getRelatedFaqId)).stream()
            .collect(Collectors.toMap(InquiryFaq::getId, Function.identity()));
        return new Lookup(categories, audiences, related);
    }

    /** 사용자에게 보이는 발행본 요약. */
    List<InquiryFaqSummaryInfo> publicSummaries(List<InquiryFaq> faqs) {
        Lookup lookup = lookup(faqs.stream().map(InquiryFaq::getPublished).toList());
        return faqs.stream().map(faq -> lookup.summary(faq.getId(), faq.getPublished())).toList();
    }

    private static Set<Long> ids(List<InquiryFaqContent> contents, Function<InquiryFaqContent, Long> id) {
        return contents.stream().map(id).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    record Lookup(
        Map<Long, InquiryCategoryInfo> categories,
        Map<Long, InquiryFaqAudienceInfo> audiences,
        Map<Long, InquiryFaq> related
    ) {

        InquiryCategoryInfo category(Long categoryId) {
            return categoryId == null ? null : categories.get(categoryId);
        }

        InquiryFaqSummaryInfo summary(Long faqId, InquiryFaqContent content) {
            return new InquiryFaqSummaryInfo(
                faqId,
                content.getQuestion(),
                category(content.getCategoryId()),
                content.getAudienceId() == null ? null : audiences.get(content.getAudienceId())
            );
        }

        /** 운영진이 보는 내용. 관련 안내는 상태와 상관없이 수정본 질문으로 보여 준다. */
        InquiryFaqContentInfo staffContent(InquiryFaqContent content) {
            if (content == null) {
                return null;
            }
            InquiryFaq relatedFaq = content.getRelatedFaqId() == null ? null : related.get(content.getRelatedFaqId());
            return new InquiryFaqContentInfo(
                content.getQuestion(),
                content.getAnswer(),
                content.getAliases(),
                category(content.getCategoryId()),
                content.getAudienceId() == null ? null : audiences.get(content.getAudienceId()),
                content.getEndDate(),
                relatedFaq == null ? null : new InquiryFaqRefInfo(relatedFaq.getId(), relatedFaq.getDraft().getQuestion())
            );
        }
    }
}
