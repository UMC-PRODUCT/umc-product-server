package com.umc.product.notice.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.convention.TestBean;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeLinksCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("공지 첨부 변경 시 수정 일시")
class NoticeContentUpdatedAtIntegrationTest extends IntegrationTestSupport {

    private static final Instant EDITED_AT = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired
    SchoolFixture schoolFixture;
    @Autowired
    SaveMemberPort saveMemberPort;
    @Autowired
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired
    SaveNoticePort saveNoticePort;
    @Autowired
    SaveNoticeTargetPort saveNoticeTargetPort;
    @Autowired
    SaveNoticeLinkPort saveNoticeLinkPort;
    @Autowired
    ManageNoticeContentUseCase manageNoticeContentUseCase;
    @Autowired
    ManageNoticeUseCase manageNoticeUseCase;
    @Autowired
    GetNoticeUseCase getNoticeUseCase;

    @TestBean(methodName = "fixedClock")
    Clock clock;

    private Long authorMemberId;

    static Clock fixedClock() {
        return Clock.fixed(EDITED_AT, ZoneOffset.UTC);
    }

    @BeforeEach
    void setUp() {
        Long schoolId = schoolFixture.학교("공지 테스트 학교").getId();
        authorMemberId = saveMemberPort.save(Member.create(
            "공지 작성자", "공지작성자", "notice-author@test.com", schoolId, null)).getId();
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(authorMemberId);
    }

    @AfterEach
    void clearAuthorityCache() {
        if (authorMemberId != null) {
            evictAuthoritySnapshotCacheUseCase.evictByMemberId(authorMemberId);
        }
    }

    @Test
    @DisplayName("링크 추가 시 첨부와 수정 일시가 함께 저장되고 작성 일시는 유지된다")
    void 링크_추가_시_수정_일시_갱신() {
        // Given
        Notice notice = saveNotice();
        Instant createdAt = getNoticeUseCase.getNoticeDetail(notice.getId(), authorMemberId).createdAt();
        List<String> links = List.of("https://example.com/notice");

        // When
        manageNoticeContentUseCase.addLinks(new AddNoticeLinksCommand(links), notice.getId(), authorMemberId);

        // Then
        NoticeInfo detail = getNoticeUseCase.getNoticeDetail(notice.getId(), authorMemberId);
        assertThat(detail.updatedAt()).isEqualTo(EDITED_AT);
        assertThat(detail.createdAt()).isEqualTo(createdAt);
        assertThat(detail.links()).extracting(NoticeLinkInfo::url).containsExactlyElementsOf(links);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("링크 교체와 빈 목록 삭제는 첨부 변경 후 수정 일시를 갱신한다")
    void 링크_교체_및_전체_삭제_시_수정_일시_갱신(boolean removeAll) {
        // Given
        Notice notice = saveNotice();
        saveNoticeLinkPort.saveAllLinks(List.of(NoticeLink.create("https://example.com/old", notice, 0)));
        List<String> links = removeAll ? List.of() : List.of("https://example.com/new");

        // When
        manageNoticeContentUseCase.replaceLinks(new ReplaceNoticeLinksCommand(links), notice.getId(), authorMemberId);

        // Then
        NoticeInfo detail = getNoticeUseCase.getNoticeDetail(notice.getId(), authorMemberId);
        assertThat(detail.updatedAt()).isEqualTo(EDITED_AT);
        assertThat(detail.links()).extracting(NoticeLinkInfo::url).containsExactlyElementsOf(links);
    }

    @Test
    @DisplayName("상세 조회와 조회수 증가는 수정 일시를 변경하지 않는다")
    void 상세_조회_및_조회수_증가_시_수정_일시_유지() {
        // Given
        Notice notice = saveNotice();
        NoticeInfo before = getNoticeUseCase.getNoticeDetail(notice.getId(), authorMemberId);

        // When
        manageNoticeUseCase.incrementViewCount(notice.getId());
        NoticeInfo after = getNoticeUseCase.getNoticeDetail(notice.getId(), authorMemberId);

        // Then
        assertThat(after.updatedAt()).isEqualTo(before.updatedAt());
        assertThat(after.viewCount()).isEqualTo(before.viewCount() + 1);
    }

    private Notice saveNotice() {
        Notice notice = saveNoticePort.save(Notice.create("제목", "내용", authorMemberId, false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(notice.getId())
            .targetGisuId(9L)
            .targetChallengerPart(List.of())
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        return notice;
    }
}
