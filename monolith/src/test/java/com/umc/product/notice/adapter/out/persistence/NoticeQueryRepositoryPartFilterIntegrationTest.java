package com.umc.product.notice.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeClassification;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.support.IntegrationTestSupport;

/**
 * 파트 개편 안전망: 지부 전체 조회(gisu+chapter, part 미지정) 시 파트 필터가 조회자 기준으로 유지되는지 검증한다.
 * <p>
 * - 관리자(회장단/중앙운영진): 파트 무관하게 전체 파트 공지가 모두 보인다. - 일반 챌린저: 본인 파트 공지 + 파트 미지정 공지만 보이고, 다른 파트 공지는 제외된다. - 신규 파트(웹/모바일 PE)가 정상
 * 매칭되는지 함께 확인한다.
 */
@DisplayName("NoticeQueryRepository 파트 필터")
class NoticeQueryRepositoryPartFilterIntegrationTest extends IntegrationTestSupport {

    private static final Long GISU_ID = 9L;
    private static final Long CHAPTER_ID = 3L;

    @Autowired
    NoticeQueryRepository noticeQueryRepository;

    @Autowired
    SaveNoticePort saveNoticePort;

    @Autowired
    SaveNoticeTargetPort saveNoticeTargetPort;

    private Long persistChallengerNotice(List<ChallengerPart> targetParts) {
        Notice notice = saveNoticePort.save(Notice.create("제목", "내용", 1L, false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(notice.getId())
            .targetGisuId(GISU_ID)
            .targetChapterId(CHAPTER_ID)
            .targetSchoolId(null)
            .targetChallengerPart(targetParts)
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        return notice.getId();
    }

    @Test
    @DisplayName("일반 챌린저는 본인 파트 공지와 파트 미지정 공지만 조회된다 (다른 파트 제외)")
    void 일반_챌린저_본인_파트만() {
        Long webNoticeId = persistChallengerNotice(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));
        Long mobileNoticeId = persistChallengerNotice(List.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER));
        Long allPartsNoticeId = persistChallengerNotice(List.of());

        NoticeClassification classification =
            new NoticeClassification(GISU_ID, CHAPTER_ID, null, null, NoticeTab.CHALLENGER);
        // viewerRole=null → 일반 챌린저, 본인 파트=웹 PE
        NoticeViewerInfo viewer =
            new NoticeViewerInfo(Set.of(ChallengerPart.WEB_PRODUCT_ENGINEER), null, CHAPTER_ID, null);

        Page<Notice> result =
            noticeQueryRepository.findByClassification(classification, viewer, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Notice::getId)
            .containsExactlyInAnyOrder(webNoticeId, allPartsNoticeId)
            .doesNotContain(mobileNoticeId);
    }

    @Test
    @DisplayName("관리자(회장단)는 지부 전체 조회 시 모든 파트 공지가 조회된다 (현행 동작 유지)")
    void 관리자_전체_파트() {
        Long webNoticeId = persistChallengerNotice(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));
        Long mobileNoticeId = persistChallengerNotice(List.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER));
        Long allPartsNoticeId = persistChallengerNotice(List.of());

        NoticeClassification classification =
            new NoticeClassification(GISU_ID, CHAPTER_ID, null, null, NoticeTab.CHALLENGER);
        // viewerRole=SCHOOL_CORE → 회장단, 파트 무관 전체 열람
        NoticeViewerInfo viewer =
            new NoticeViewerInfo(Set.of(), null, CHAPTER_ID, NoticeTab.SCHOOL_CORE);

        Page<Notice> result =
            noticeQueryRepository.findByClassification(classification, viewer, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Notice::getId)
            .containsExactlyInAnyOrder(webNoticeId, mobileNoticeId, allPartsNoticeId);
    }
}
