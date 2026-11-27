package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.service.query.NoticeReadAudienceService.Reader;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

@ExtendWith(MockitoExtension.class)
class NoticeReadStatusPageTest {

    @Mock
    private NoticeReadAudienceService audienceService;
    @Mock
    private GetMemberUseCase getMemberUseCase;
    @InjectMocks
    private NoticeQueryService service;

    @ParameterizedTest
    @EnumSource(NoticeReadStatus.class)
    @DisplayName("학교·지부와 읽음 상태로 필터링하고, 페이지 마지막 회원이 탈퇴해도 다음 커서로 이어간다")
    void paginate(NoticeReadStatus status) {
        boolean read = status == NoticeReadStatus.READ;
        NoticeReadStatusFilterType filter = read
            ? NoticeReadStatusFilterType.SCHOOL : NoticeReadStatusFilterType.CHAPTER;
        List<Long> organizationIds = List.of(read ? 10L : 30L);
        List<Reader> readers = new ArrayList<>();
        for (long id = 21; id > 0; id--) {
            readers.add(reader(id, read, 10L, 30L));
        }
        readers.add(reader(22L, !read, 10L, 30L));
        readers.add(reader(23L, read, 11L, 31L));
        given(audienceService.listReaders(1L)).willReturn(readers);
        given(getMemberUseCase.findAllByIds(anySet())).willAnswer(call -> {
            Set<Long> memberIds = call.getArgument(0);
            return memberIds.stream().filter(id -> id != 20L)
                .collect(Collectors.toMap(Function.identity(), id -> MemberInfo.builder().id(id).build()));
        });

        NoticeReadStatusResult first = service.getReadStatus(new GetNoticeStatusQuery(
            null, 1L, filter, organizationIds, status));
        NoticeReadStatusResult second = service.getReadStatus(new GetNoticeStatusQuery(
            first.cursorId(), 1L, filter, organizationIds, status));

        assertThat(first.content()).extracting(NoticeReadStatusInfo::challengerId)
            .containsExactlyElementsOf(LongStream.rangeClosed(1, 19).boxed().toList());
        assertThat(first.hasNext()).isTrue();
        assertThat(first.cursorId()).isEqualTo(read ? 120L : 20L);
        assertThat(second.content()).extracting(NoticeReadStatusInfo::challengerId).containsExactly(21L);
        assertThat(second.hasNext()).isFalse();
        assertThat(second.cursorId()).isEqualTo(read ? 121L : 21L);
    }

    private Reader reader(long id, boolean read, long school, long chapter) {
        ChallengerBasicInfo challenger = new ChallengerBasicInfo(id, id, 1L, ChallengerPart.PLAN, false,
            ChallengerStatus.ACTIVE);
        return new Reader(challenger, school, new ChapterInfo(chapter, "지부"), read ? id + 100L : null);
    }
}
