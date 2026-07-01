package com.umc.product.member.adapter.in.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.global.config.GraphQlRuntimeWiringConfig;
import com.umc.product.global.exception.GraphQlExceptionAdvice;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;

@GraphQlTest(MemberGraphQlController.class)
@Import({GraphQlRuntimeWiringConfig.class, GraphQlExceptionAdvice.class})
@DisplayName("MemberGraphQlController")
class MemberGraphQlControllerTest {

    private static final Long REQUESTER_ID = 1L;
    private static final Long TARGET_ID = 2L;

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    GetMemberUseCase getMemberUseCase;

    @MockitoBean
    CheckPermissionUseCase checkPermissionUseCase;

    @MockitoBean
    GetSchoolUseCase getSchoolUseCase;

    @MockitoBean
    GetChallengerUseCase getChallengerUseCase;

    @MockitoBean
    GetGisuUseCase getGisuUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new MemberPrincipal(REQUESTER_ID), null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("me는 인증된 본인 회원 정보를 private view로 조회한다")
    void me는_인증된_본인_회원_정보를_private_view로_조회한다() {
        given(getMemberUseCase.getById(REQUESTER_ID)).willReturn(memberInfo(REQUESTER_ID));

        graphQlTester.document("""
                query {
                  me {
                    memberId
                    name
                    nickname
                    email
                    schoolId
                    schoolName
                    profileImageLink
                    status
                  }
                }
                """)
            .execute()
            .path("me.memberId").entity(String.class).isEqualTo("1")
            .path("me.email").entity(String.class).isEqualTo("member1@example.com")
            .path("me.status").entity(String.class).isEqualTo("ACTIVE");

        then(checkPermissionUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("member는 MEMBER READ 권한을 먼저 검사하고 public view로 조회한다")
    void member는_MEMBER_READ_권한을_먼저_검사하고_public_view로_조회한다() {
        given(getMemberUseCase.getById(TARGET_ID)).willReturn(memberInfo(TARGET_ID));

        graphQlTester.document("""
                query {
                  member(id: 2) {
                    memberId
                    name
                    nickname
                    email
                    status
                  }
                }
                """)
            .execute()
            .path("member.memberId").entity(String.class).isEqualTo("2")
            .path("member.name").entity(String.class).isEqualTo("member2")
            .path("member.email").valueIsNull()
            .path("member.status").valueIsNull();

        then(checkPermissionUseCase).should().checkOrThrow(REQUESTER_ID, memberReadPermission(TARGET_ID));
        then(getMemberUseCase).should().getById(TARGET_ID);
    }

    @Test
    @DisplayName("member 권한이 거부되면 회원 조회 usecase를 호출하지 않는다")
    void member_권한이_거부되면_회원_조회_usecase를_호출하지_않는다() {
        willThrow(new AccessDeniedException("회원 정보를 볼 권한이 없어요."))
            .given(checkPermissionUseCase)
            .checkOrThrow(REQUESTER_ID, memberReadPermission(TARGET_ID));

        graphQlTester.document("""
                query {
                  member(id: 2) {
                    memberId
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertCommonError(errors, "member", CommonErrorCode.FORBIDDEN));

        then(getMemberUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("me는 인증 정보가 없으면 회원 조회 usecase를 호출하지 않는다")
    void me는_인증_정보가_없으면_회원_조회_usecase를_호출하지_않는다() {
        SecurityContextHolder.clearContext();

        graphQlTester.document("""
                query {
                  me {
                    memberId
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertCommonError(errors, "me", CommonErrorCode.FORBIDDEN));

        then(getMemberUseCase).shouldHaveNoInteractions();
        then(checkPermissionUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("members는 ID를 중복 제거하고 권한 확인 후 batch 조회한다")
    void members는_ID를_중복_제거하고_권한_확인_후_batch_조회한다() {
        SubjectAttributes subject = subject();
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(checkPermissionUseCase.check(subject, memberReadPermission(2L))).willReturn(true);
        given(checkPermissionUseCase.check(subject, memberReadPermission(3L))).willReturn(true);
        given(getMemberUseCase.findAllByIds(Set.of(2L, 3L))).willReturn(Map.of(
            2L, memberInfo(2L),
            3L, memberInfo(3L)
        ));

        graphQlTester.document("""
                query {
                  members(ids: [2, 2, 3]) {
                    memberId
                    email
                    status
                  }
                }
                """)
            .execute()
            .path("members[0].memberId").entity(String.class).isEqualTo("2")
            .path("members[0].email").valueIsNull()
            .path("members[0].status").valueIsNull()
            .path("members[1].memberId").entity(String.class).isEqualTo("3");

        then(getMemberUseCase).should().findAllByIds(Set.of(2L, 3L));
    }

    @Test
    @DisplayName("members는 school과 challengers와 gisu를 nested field로 batch 조회한다")
    void members는_school과_challengers와_gisu를_nested_field로_batch_조회한다() {
        SubjectAttributes subject = subject();
        LinkedHashSet<Long> memberIds = new LinkedHashSet<>(List.of(2L, 3L));
        LinkedHashSet<Long> schoolIds = new LinkedHashSet<>(List.of(10L, 11L));
        LinkedHashSet<Long> gisuIds = new LinkedHashSet<>(List.of(100L, 101L));
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(checkPermissionUseCase.check(subject, memberReadPermission(2L))).willReturn(true);
        given(checkPermissionUseCase.check(subject, memberReadPermission(3L))).willReturn(true);
        given(getMemberUseCase.findAllByIds(memberIds)).willReturn(Map.of(
            2L, memberInfo(2L, 10L),
            3L, memberInfo(3L, 11L)
        ));
        given(getSchoolUseCase.listDetailsByIds(schoolIds)).willReturn(List.of(
            school(10L, "중앙대학교"),
            school(11L, "숭실대학교")
        ));
        given(getChallengerUseCase.getAllBasicByMemberIds(memberIds)).willReturn(Map.of(
            2L, List.of(challenger(20L, 2L, 100L, ChallengerPart.SPRINGBOOT, ChallengerStatus.ACTIVE)),
            3L, List.of(challenger(30L, 3L, 101L, ChallengerPart.DESIGN, ChallengerStatus.GRADUATED))
        ));
        given(getGisuUseCase.getByIds(gisuIds)).willReturn(List.of(
            gisu(100L, 6L),
            gisu(101L, 7L)
        ));

        graphQlTester.document("""
                query {
                  members(ids: [2, 3]) {
                    memberId
                    school {
                      schoolId
                      schoolName
                    }
                    challengers {
                      challengerId
                      part
                      status
                      gisu {
                        gisuId
                        generation
                      }
                    }
                  }
                }
                """)
            .execute()
            .path("members[0].school.schoolName").entity(String.class).isEqualTo("중앙대학교")
            .path("members[0].challengers[0].part").entity(String.class).isEqualTo("SPRINGBOOT")
            .path("members[0].challengers[0].status").entity(String.class).isEqualTo("ACTIVE")
            .path("members[0].challengers[0].gisu.generation").entity(String.class).isEqualTo("6")
            .path("members[1].school.schoolName").entity(String.class).isEqualTo("숭실대학교")
            .path("members[1].challengers[0].status").entity(String.class).isEqualTo("GRADUATED")
            .path("members[1].challengers[0].gisu.generation").entity(String.class).isEqualTo("7");

        then(getSchoolUseCase).should().listDetailsByIds(schoolIds);
        then(getChallengerUseCase).should().getAllBasicByMemberIds(memberIds);
        then(getGisuUseCase).should().getByIds(gisuIds);
    }

    @Test
    @DisplayName("members 중 하나라도 권한이 없으면 batch 조회를 호출하지 않는다")
    void members_중_하나라도_권한이_없으면_batch_조회를_호출하지_않는다() {
        SubjectAttributes subject = subject();
        given(checkPermissionUseCase.loadSubject(REQUESTER_ID)).willReturn(subject);
        given(checkPermissionUseCase.check(subject, memberReadPermission(2L))).willReturn(true);
        given(checkPermissionUseCase.check(subject, memberReadPermission(3L))).willReturn(false);

        graphQlTester.document("""
                query {
                  members(ids: [2, 3]) {
                    memberId
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertCommonError(errors, "members", CommonErrorCode.FORBIDDEN));

        then(getMemberUseCase).shouldHaveNoInteractions();
        then(getSchoolUseCase).shouldHaveNoInteractions();
        then(getChallengerUseCase).shouldHaveNoInteractions();
        then(getGisuUseCase).shouldHaveNoInteractions();
    }

    private void assertCommonError(
        List<org.springframework.graphql.ResponseError> errors,
        String path,
        CommonErrorCode code
    ) {
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getPath()).isEqualTo(path);
        assertThat(errors.get(0).getExtensions()).containsEntry("code", code.getCode());
    }

    private MemberInfo memberInfo(Long memberId) {
        return memberInfo(memberId, 10L);
    }

    private MemberInfo memberInfo(Long memberId, Long schoolId) {
        return MemberInfo.builder()
            .id(memberId)
            .name("member" + memberId)
            .nickname("nick" + memberId)
            .email("member" + memberId + "@example.com")
            .schoolId(schoolId)
            .schoolName("중앙대학교")
            .profileImageId("profile-" + memberId)
            .profileImageLink("https://cdn.example.com/profile-" + memberId + ".png")
            .status(MemberStatus.ACTIVE)
            .roles(List.of())
            .build();
    }

    private SubjectAttributes subject() {
        return SubjectAttributes.builder()
            .memberId(REQUESTER_ID)
            .schoolId(10L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of())
            .build();
    }

    private ResourcePermission memberReadPermission(Long memberId) {
        return ResourcePermission.of(ResourceType.MEMBER, memberId, PermissionType.READ);
    }

    private SchoolDetailInfo school(Long schoolId, String schoolName) {
        return new SchoolDetailInfo(
            1L,
            "1지부",
            schoolName,
            schoolId,
            "비고",
            null,
            List.of(),
            true,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-02T00:00:00Z")
        );
    }

    private ChallengerBasicInfo challenger(
        Long challengerId,
        Long memberId,
        Long gisuId,
        ChallengerPart part,
        ChallengerStatus status
    ) {
        return new ChallengerBasicInfo(challengerId, memberId, gisuId, part, status);
    }

    private GisuInfo gisu(Long gisuId, Long generation) {
        return new GisuInfo(
            gisuId,
            generation,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T00:00:00Z"),
            true
        );
    }
}
