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
import com.umc.product.common.domain.enums.ChallengerRoleType;
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

    @Test
    void UMC_전체와_검색은_본인_권한_내_지부_학교_파트_공지를_함께_조회한다() {
        // given
        Long global = persistNotice(null, null, List.of(), NoticeTab.CHALLENGER);
        Long chapter = persistNotice(CHAPTER_ID, null, List.of(), NoticeTab.CHALLENGER);
        Long schoolWeb = persistNotice(null, 5L, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.CHALLENGER);
        Long schoolMobile = persistNotice(null, 5L, List.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER), NoticeTab.CHALLENGER);
        Long otherSchool = persistNotice(null, 6L, List.of(), NoticeTab.CHALLENGER);
        Long otherChapter = persistNotice(4L, null, List.of(), NoticeTab.CHALLENGER);
        NoticeClassification classification = new NoticeClassification(GISU_ID, null, null, null, NoticeTab.CHALLENGER);
        NoticeViewerInfo viewer = new NoticeViewerInfo(Set.of(ChallengerPart.WEB_PRODUCT_ENGINEER), 5L, CHAPTER_ID, null);

        // when
        Page<Notice> list = noticeQueryRepository.findByClassification(classification, viewer, PageRequest.of(0, 10));
        Page<Notice> search = noticeQueryRepository.findByKeyword("제목", classification, viewer, PageRequest.of(0, 10));

        // then
        assertThat(list.getContent()).extracting(Notice::getId).containsExactlyInAnyOrder(global, chapter, schoolWeb)
            .doesNotContain(schoolMobile, otherSchool, otherChapter);
        assertThat(search.getContent()).extracting(Notice::getId).containsExactlyElementsOf(
            list.getContent().stream().map(Notice::getId).toList());
        assertThat(list.getTotalElements()).isEqualTo(3);
        assertThat(search.getTotalElements()).isEqualTo(3);
    }

    @Test
    void 중앙_운영진의_UMC_전체에는_모든_지부_학교_파트_공지를_포함한다() {
        // given
        Long chapter = persistNotice(4L, null, List.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER), NoticeTab.CHALLENGER);
        Long school = persistNotice(null, 6L, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.CHALLENGER);
        NoticeClassification classification = new NoticeClassification(GISU_ID, null, null, null, NoticeTab.CHALLENGER);
        NoticeViewerInfo viewer = new NoticeViewerInfo(Set.of(), 5L, CHAPTER_ID, NoticeTab.CENTRAL_MEMBER);
        // when
        Page<Notice> result = noticeQueryRepository.findByClassification(classification, viewer, PageRequest.of(0, 1));
        // then: 페이지 크기와 전체 개수 유지
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).extracting(Notice::getId).containsAnyOf(chapter, school);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void 지부장은_본인_지부_학교_챌린저_공지를_읽지만_학교_운영진_공지는_조회하지_못한다() {
        // given
        Long ownSchool = persistNotice(null, 5L, List.of(), NoticeTab.CHALLENGER);
        Long otherSchool = persistNotice(null, 6L, List.of(), NoticeTab.CHALLENGER);
        Long centralStaff = persistNotice(null, null, List.of(), NoticeTab.SCHOOL_CORE);
        Long schoolStaff = persistNotice(null, 5L, List.of(), NoticeTab.SCHOOL_PART_LEADER);
        Long centralOnly = persistNotice(null, null, List.of(), NoticeTab.CENTRAL_MEMBER);
        NoticeViewerInfo viewer = new NoticeViewerInfo(Set.of(), 5L, CHAPTER_ID, NoticeTab.SCHOOL_CORE,
            ChallengerRoleType.CHAPTER_PRESIDENT, Set.of(5L));
        // when
        Page<Notice> challenger = noticeQueryRepository.findByClassification(
            new NoticeClassification(GISU_ID, null, null, null, NoticeTab.CHALLENGER), viewer, PageRequest.of(0, 10));
        Page<Notice> staff = noticeQueryRepository.findByClassification(
            new NoticeClassification(GISU_ID, null, null, null, NoticeTab.SCHOOL_CORE), viewer, PageRequest.of(0, 10));
        // then
        assertThat(challenger.getContent()).extracting(Notice::getId).containsExactly(ownSchool).doesNotContain(otherSchool);
        assertThat(staff.getContent()).extracting(Notice::getId).containsExactly(centralStaff)
            .doesNotContain(schoolStaff, centralOnly);
    }

    private Long persistNotice(Long chapterId, Long schoolId, List<ChallengerPart> parts, NoticeTab tab) {
        Notice notice = saveNoticePort.save(Notice.create("제목", "내용", 1L, false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder().noticeId(notice.getId()).targetGisuId(GISU_ID)
            .targetChapterId(chapterId).targetSchoolId(schoolId).targetChallengerPart(parts).targetNoticeTab(tab).build());
        return notice.getId();
    }
}
