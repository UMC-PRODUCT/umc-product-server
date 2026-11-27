package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.member.application.port.out.WithdrawnMemberIdentityPort;
import com.umc.product.member.application.service.MemberReadmissionIdentityService;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberRejoinKey;
import com.umc.product.member.domain.WithdrawnMemberIdentity;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({WithdrawnMemberIdentityPersistenceAdapter.class, MemberReadmissionIdentityService.class})
@DisplayName("탈부 재가입 이메일 식별 정보 저장소")
class WithdrawnMemberIdentityPersistenceAdapterTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    WithdrawnMemberIdentityPort identities;

    @Autowired
    MemberReadmissionIdentityService service;

    @Test
    @DisplayName("로컬 부분의 대소문자는 보존하고 도메인 대소문자가 다른 현재 및 과거 계정은 합친다")
    void findActiveAndFormerMemberIdsWithMatchingEmail() {
        // given
        Member current = entityManager.persist(Member.create("회원", "회원", "Local@test.COM", null, null));
        Member different = entityManager.persist(Member.create("다른 회원", "다름", "local@test.com", null, null));
        entityManager.flush();
        Long formerMemberId = current.getId() + 1_000L;
        WithdrawnMemberIdentity retained = WithdrawnMemberIdentity.create(
            formerMemberId, MemberRejoinKey.fromVerifiedEmail("Local@TEST.com")
        );
        identities.saveIfAbsent(retained);
        identities.saveIfAbsent(retained);

        // when / then
        assertThat(service.findMemberIds(" Local@TEST.COM "))
            .containsExactlyInAnyOrder(current.getId(), formerMemberId);
        assertThat(service.findMemberIds("local@test.com")).containsExactly(different.getId());

        service.lockByFormerMemberId(formerMemberId);
        service.deleteByFormerMemberId(formerMemberId);
        assertThat(service.findMemberIds("Local@test.com")).containsExactly(current.getId());
    }
}
