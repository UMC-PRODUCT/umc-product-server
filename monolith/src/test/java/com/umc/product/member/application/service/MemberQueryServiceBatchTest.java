package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberQueryService batch 조회")
class MemberQueryServiceBatchTest {

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    GetSchoolUseCase getSchoolUseCase;

    @Mock
    GetFileUseCase getFileUseCase;

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @InjectMocks
    MemberQueryService sut;

    @Test
    @DisplayName("findAllByIds는 빈 입력이면 외부 조회 없이 빈 Map을 반환한다")
    void findAllByIdsReturnsEmptyMapWithoutExternalLookup() {
        assertThat(sut.findAllByIds(Set.of())).isEmpty();
        assertThat(sut.findAllByIds(null)).isEmpty();

        then(loadMemberPort).should(never()).findAllByIds(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("이메일 일괄 조회는 빈 입력이면 저장소를 조회하지 않는다")
    void findIdsByEmailsReturnsEmptyMapWithoutLookup() {
        assertThat(sut.findIdsByEmails(Set.of())).isEmpty();
        assertThat(sut.findIdsByEmails(null)).isEmpty();

        then(loadMemberPort).should(never()).findIdsByEmails(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("이메일 일괄 조회는 있는 이메일의 회원 ID만 돌려준다")
    void findIdsByEmailsReturnsFoundIds() {
        given(loadMemberPort.findIdsByEmails(Set.of("a@test.com", "missing@test.com")))
            .willReturn(Map.of("a@test.com", 1L));

        Map<String, Long> result = sut.findIdsByEmails(Set.of("a@test.com", "missing@test.com"));

        assertThat(result).containsExactly(Map.entry("a@test.com", 1L));
    }

    @Test
    @DisplayName("findAllByIds는 같은 schoolId와 profileImageId를 한 번만 조회한다")
    void findAllByIdsCachesRepeatedSchoolAndProfileLookups() {
        Member first = member(1L, "홍길동", 10L, "profile-file-id");
        Member second = member(2L, "김철수", 10L, "profile-file-id");
        given(loadMemberPort.findAllByIds(Set.of(1L, 2L))).willReturn(List.of(first, second));
        given(getSchoolUseCase.getSchoolDetail(10L)).willReturn(school(10L, "테스트대학교"));
        given(getFileUseCase.getById("profile-file-id")).willReturn(file("profile-file-id"));

        Map<Long, MemberInfo> result = sut.findAllByIds(Set.of(1L, 2L));

        assertThat(result).containsKeys(1L, 2L);
        assertThat(result.get(1L).schoolName()).isEqualTo("테스트대학교");
        assertThat(result.get(2L).profileImageLink()).isEqualTo("https://cdn.example.com/profile-file-id");
        then(getSchoolUseCase).should(times(1)).getSchoolDetail(10L);
        then(getFileUseCase).should(times(1)).getById("profile-file-id");
    }

    @Test
    void 학교가_없는_회원도_학교조회_없이_정보를_반환한다() {
        // given
        Member member = member(1L, "최고관리자", null, null);
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        // when
        var result = sut.getById(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.schoolId()).isNull();
        assertThat(result.schoolName()).isNull();
        then(getSchoolUseCase).shouldHaveNoInteractions();
        then(getFileUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 회원_기본정보는_학교와_프로필파일_역할_추가조회_없이_반환한다() {
        // given
        Member member = member(1L, "홍길동", 10L, "누락된-프로필파일");
        given(loadMemberPort.findAllByIds(Set.of(1L))).willReturn(List.of(member));

        // when
        var result = sut.findAllBasicByIds(Set.of(1L));

        // then
        assertThat(result.get(1L).memberId()).isEqualTo(1L);
        assertThat(result.get(1L).name()).isEqualTo("홍길동");
        assertThat(result.get(1L).nickname()).isEqualTo("홍길");
        assertThat(result.get(1L).schoolId()).isEqualTo(10L);
        assertThat(sut.findAllBasicByIds(Set.of())).isEmpty();
        assertThat(sut.findAllBasicByIds(null)).isEmpty();
        then(getSchoolUseCase).shouldHaveNoInteractions();
        then(getFileUseCase).shouldHaveNoInteractions();
        then(getChallengerRoleUseCase).shouldHaveNoInteractions();
        then(loadMemberPort).should(times(1)).findAllByIds(Set.of(1L));
    }

    @Test
    @DisplayName("회원명 일괄 조회는 부가 프로필 조회 없이 ID와 이름만 반환한다")
    void findAllNamesByIdsReturnsOnlyIdsAndNames() {
        Member first = member(1L, "홍길동", 10L, "profile-file-id");
        Member second = member(2L, "김철수", 20L, null);
        given(loadMemberPort.findAllByIds(Set.of(1L, 2L))).willReturn(List.of(first, second));

        Map<Long, String> result = sut.findAllNamesByIds(Set.of(1L, 2L));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(1L, "홍길동", 2L, "김철수"));
        then(getSchoolUseCase).shouldHaveNoInteractions();
        then(getFileUseCase).shouldHaveNoInteractions();
        then(getChallengerRoleUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원명 검색은 후보 회원 안에서 앞뒤 공백을 제거한 이름으로 수행한다")
    void searchIdsByNameWithinCandidateMembers() {
        given(loadMemberPort.searchIdsByName(Set.of(1L, 2L), "길동")).willReturn(Set.of(1L));

        Set<Long> result = sut.searchIdsByName(Set.of(1L, 2L), "  길동  ");

        assertThat(result).containsExactly(1L);
        then(loadMemberPort).should().searchIdsByName(Set.of(1L, 2L), "길동");
    }

    @Test
    @DisplayName("listIdsBySchoolIds는 조회 결과가 없는 학교도 빈 Set으로 채운다")
    void listIdsBySchoolIdsFillsMissingSchoolsWithEmptySet() {
        given(loadMemberPort.listIdsBySchoolIds(Set.of(10L, 20L)))
            .willReturn(Map.of(10L, Set.of(1L, 2L)));

        Map<Long, Set<Long>> result = sut.listIdsBySchoolIds(Set.of(10L, 20L));

        assertThat(result.get(10L)).containsExactlyInAnyOrder(1L, 2L);
        assertThat(result.get(20L)).isEmpty();
    }

    private Member member(Long id, String name, Long schoolId, String profileImageId) {
        Member member = Member.create(name, name.substring(0, 2), name + "@example.com", schoolId, profileImageId);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private SchoolDetailInfo school(Long schoolId, String schoolName) {
        return new SchoolDetailInfo(
            1L,
            "서울",
            schoolName,
            null,
            schoolId,
            null,
            null,
            List.of(),
            true,
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private FileInfo file(String fileId) {
        return new FileInfo(
            fileId,
            "profile.png",
            null,
            "image/png",
            100L,
            "https://cdn.example.com/" + fileId,
            true,
            1L,
            Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
