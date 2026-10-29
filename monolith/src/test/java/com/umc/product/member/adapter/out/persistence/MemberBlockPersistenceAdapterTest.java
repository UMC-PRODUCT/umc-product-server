package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.domain.MemberBlock;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberBlockPersistenceAdapter")
class MemberBlockPersistenceAdapterTest {

    @Mock
    MemberBlockJpaRepository memberBlockJpaRepository;

    MemberBlockPersistenceAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new MemberBlockPersistenceAdapter(memberBlockJpaRepository);
    }

    @Test
    @DisplayName("listBlockedMemberIdsByBlockerIds는 빈 입력이면 레포지토리를 호출하지 않는다")
    void listBlockedMemberIdsByBlockerIds_빈_입력이면_조회하지_않는다() {
        Map<Long, Set<Long>> result = sut.listBlockedMemberIdsByBlockerIds(Set.of());

        assertThat(result).isEmpty();
        then(memberBlockJpaRepository).should(never()).findAllByBlockerMemberIdIn(org.mockito.ArgumentMatchers.anySet());
    }

    @Test
    @DisplayName("listBlockedMemberIdsByBlockerIds는 한 번의 조회 결과를 blockerMemberId별로 묶는다")
    void listBlockedMemberIdsByBlockerIds_blockerMemberId별로_묶는다() {
        Set<Long> blockerIds = Set.of(1L, 2L);
        given(memberBlockJpaRepository.findAllByBlockerMemberIdIn(blockerIds)).willReturn(List.of(
            MemberBlock.create(1L, 10L),
            MemberBlock.create(1L, 20L),
            MemberBlock.create(2L, 30L)
        ));

        Map<Long, Set<Long>> result = sut.listBlockedMemberIdsByBlockerIds(blockerIds);

        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L)).containsExactlyInAnyOrder(10L, 20L);
        assertThat(result.get(2L)).containsExactly(30L);
        then(memberBlockJpaRepository).should().findAllByBlockerMemberIdIn(blockerIds);
    }

    @Test
    @DisplayName("delete는 blocker/blocked id로 레포지토리 삭제를 호출한다")
    void delete_레포지토리_삭제를_호출한다() {
        sut.delete(1L, 2L);

        then(memberBlockJpaRepository).should().deleteByBlockerMemberIdAndBlockedMemberId(1L, 2L);
    }
}
