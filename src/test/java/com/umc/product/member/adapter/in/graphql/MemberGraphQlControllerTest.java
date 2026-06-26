package com.umc.product.member.adapter.in.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

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
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.global.config.GraphQlRuntimeWiringConfig;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;

@GraphQlTest(MemberGraphQlController.class)
@Import(GraphQlRuntimeWiringConfig.class)
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
            .satisfy(errors -> assertThat(errors).isNotEmpty());

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
            .satisfy(errors -> assertThat(errors).isNotEmpty());

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
            .satisfy(errors -> assertThat(errors).isNotEmpty());

        then(getMemberUseCase).shouldHaveNoInteractions();
    }

    private MemberInfo memberInfo(Long memberId) {
        return MemberInfo.builder()
            .id(memberId)
            .name("member" + memberId)
            .nickname("nick" + memberId)
            .email("member" + memberId + "@example.com")
            .schoolId(10L)
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
}
