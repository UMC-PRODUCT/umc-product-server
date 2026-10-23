package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingChapterTransferPort;
import com.umc.product.project.application.service.policy.ProjectMatchingChapterResolver.ChapterTransferDelta;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingChapterResolverTest {

    private static final Long GISU_ID = 11L;
    private static final Long MEMBER_ID = 1L;
    private static final Long SCHOOL_ID = 30L;
    private static final Long SEOUL = 3L;
    private static final Long GYEONGGI = 5L;

    @Mock
    LoadProjectMatchingChapterTransferPort loadTransferPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @InjectMocks
    ProjectMatchingChapterResolver sut;

    @Nested
    class findMatchingChapterId {

        @Test
        @DisplayName("활성 이동이 없으면 학교 기준 지부를 매칭 지부로 쓴다")
        void 이동이_없으면_원지부() {
            given(loadTransferPort.findActiveByGisuIdAndMemberId(GISU_ID, MEMBER_ID)).willReturn(Optional.empty());
            givenOriginChapter(SEOUL);

            assertThat(sut.findMatchingChapterId(MEMBER_ID, GISU_ID)).contains(SEOUL);
        }

        @Test
        @DisplayName("활성 이동이 있으면 학교를 조회하지 않고 이동지부를 매칭 지부로 쓴다")
        void 이동이_있으면_이동지부() {
            given(loadTransferPort.findActiveByGisuIdAndMemberId(GISU_ID, MEMBER_ID))
                .willReturn(Optional.of(transfer(MEMBER_ID, SEOUL, GYEONGGI)));

            assertThat(sut.findMatchingChapterId(MEMBER_ID, GISU_ID)).contains(GYEONGGI);
            then(getMemberUseCase).should(never()).getById(MEMBER_ID);
        }

        @Test
        @DisplayName("학교가 지부에 배정되지 않았고 이동도 없으면 매칭 지부가 없다")
        void 지부_배정이_없으면_비어있다() {
            given(loadTransferPort.findActiveByGisuIdAndMemberId(GISU_ID, MEMBER_ID)).willReturn(Optional.empty());
            given(getMemberUseCase.getById(MEMBER_ID)).willReturn(member(SCHOOL_ID));
            given(getChapterUseCase.findByGisuAndSchool(GISU_ID, SCHOOL_ID)).willReturn(Optional.empty());

            assertThat(sut.findMatchingChapterId(MEMBER_ID, GISU_ID)).isEmpty();
            assertThat(sut.isInMatchingChapter(MEMBER_ID, GISU_ID, SEOUL)).isFalse();
        }
    }

    @Nested
    class deltaOf {

        @Test
        @DisplayName("지부 인원 보정은 나간 챌린저를 빼고 들어온 챌린저를 더한다")
        void 나간_사람은_빼고_들어온_사람은_더한다() {
            Long stayingMember = 1L;
            Long leavingMember = 2L;
            Long joiningMember = 3L;
            given(loadTransferPort.listActiveByChapterId(SEOUL)).willReturn(List.of(
                transfer(leavingMember, SEOUL, GYEONGGI),
                transfer(joiningMember, GYEONGGI, SEOUL)
            ));
            given(getChallengerUseCase.listByMemberIdsAndGisuId(Set.of(joiningMember), GISU_ID))
                .willReturn(Map.of(joiningMember, challenger(joiningMember)));

            ChapterTransferDelta delta = sut.deltaOf(SEOUL);
            List<ChallengerInfo> adjusted = delta.apply(List.of(challenger(stayingMember), challenger(leavingMember)));

            assertThat(adjusted).extracting(ChallengerInfo::memberId)
                .containsExactly(stayingMember, joiningMember);
        }

        @Test
        @DisplayName("이동 기록이 없으면 학교 기준 인원을 그대로 쓴다")
        void 이동이_없으면_그대로() {
            given(loadTransferPort.listActiveByChapterId(SEOUL)).willReturn(List.of());
            List<ChallengerInfo> schoolBased = List.of(challenger(1L), challenger(2L));

            assertThat(sut.deltaOf(SEOUL).apply(schoolBased)).isSameAs(schoolBased);
            then(getChallengerUseCase).should(never()).listByMemberIdsAndGisuId(any(), any());
        }
    }

    private void givenOriginChapter(Long chapterId) {
        given(getMemberUseCase.getById(MEMBER_ID)).willReturn(member(SCHOOL_ID));
        given(getChapterUseCase.findByGisuAndSchool(GISU_ID, SCHOOL_ID))
            .willReturn(Optional.of(new ChapterInfo(chapterId, "지부")));
    }

    private static MemberInfo member(Long schoolId) {
        return MemberInfo.builder().id(MEMBER_ID).schoolId(schoolId).build();
    }

    private static ProjectMatchingChapterTransfer transfer(Long memberId, Long from, Long to) {
        return ProjectMatchingChapterTransfer.create(GISU_ID, memberId, from, to, null, 99L);
    }

    private static ChallengerInfo challenger(Long memberId) {
        return ChallengerInfo.builder()
            .challengerId(memberId * 10)
            .memberId(memberId)
            .gisuId(GISU_ID)
            .part(ChallengerPart.DESIGN)
            .challengerStatus(ChallengerStatus.ACTIVE)
            .build();
    }
}
