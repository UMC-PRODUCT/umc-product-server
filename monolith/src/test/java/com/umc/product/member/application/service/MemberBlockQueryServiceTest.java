package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;
import com.umc.product.member.application.port.out.LoadMemberBlockPort;
import com.umc.product.member.domain.MemberBlock;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberBlockQueryService")
class MemberBlockQueryServiceTest {

    private static final Long BLOCKER_ID = 10L;

    @Mock
    LoadMemberBlockPort loadMemberBlockPort;

    @Mock
    com.umc.product.member.application.port.in.query.GetMemberUseCase getMemberUseCase;

    MemberBlockQueryService sut;

    @BeforeEach
    void setUp() {
        sut = new MemberBlockQueryService(loadMemberBlockPort, getMemberUseCase);
    }

    @Test
    @DisplayName("listBlockedMembers는 차단 목록에 이름을 채워 반환한다")
    void listBlockedMembers_이름을_채워_반환한다() {
        Instant blockedAt = Instant.parse("2026-10-29T00:00:00Z");
        MemberBlock block = MemberBlock.create(BLOCKER_ID, 20L);
        given(loadMemberBlockPort.listByBlockerMemberId(BLOCKER_ID)).willReturn(List.of(block));
        given(getMemberUseCase.findAllNamesByIds(Set.of(20L))).willReturn(Map.of(20L, "홍길동"));
        org.springframework.test.util.ReflectionTestUtils.setField(block, "createdAt", blockedAt);

        List<BlockedMemberInfo> result = sut.listBlockedMembers(BLOCKER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).memberId()).isEqualTo(20L);
        assertThat(result.get(0).name()).isEqualTo("홍길동");
        assertThat(result.get(0).blockedAt()).isEqualTo(blockedAt);
    }

    @Test
    @DisplayName("listBlockedMembers는 차단한 대상이 없으면 빈 목록을 반환하고 이름 조회를 하지 않는다")
    void listBlockedMembers_차단_대상이_없으면_빈_목록을_반환한다() {
        given(loadMemberBlockPort.listByBlockerMemberId(BLOCKER_ID)).willReturn(List.of());

        List<BlockedMemberInfo> result = sut.listBlockedMembers(BLOCKER_ID);

        assertThat(result).isEmpty();
        then(getMemberUseCase).should(never()).findAllNamesByIds(anySet());
    }

    @Test
    @DisplayName("getBlockedMemberIds는 차단한 회원 id 집합을 반환한다")
    void getBlockedMemberIds_차단한_회원_id_집합을_반환한다() {
        given(loadMemberBlockPort.listByBlockerMemberId(BLOCKER_ID))
            .willReturn(List.of(MemberBlock.create(BLOCKER_ID, 20L), MemberBlock.create(BLOCKER_ID, 30L)));

        Set<Long> result = sut.getBlockedMemberIds(BLOCKER_ID);

        assertThat(result).containsExactlyInAnyOrder(20L, 30L);
    }

    @Test
    @DisplayName("getBlockedMemberIdsBatch는 빈 입력이면 포트를 호출하지 않고 빈 Map을 반환한다")
    void getBlockedMemberIdsBatch_빈_입력이면_빈_결과를_반환한다() {
        Map<Long, Set<Long>> result = sut.getBlockedMemberIdsBatch(Set.of());

        assertThat(result).isEmpty();
        then(loadMemberBlockPort).should(never()).listBlockedMemberIdsByBlockerIds(anySet());
    }

    @Test
    @DisplayName("getBlockedMemberIdsBatch는 포트 결과를 그대로 반환한다")
    void getBlockedMemberIdsBatch_포트_결과를_반환한다() {
        Set<Long> blockerIds = Set.of(BLOCKER_ID);
        given(loadMemberBlockPort.listBlockedMemberIdsByBlockerIds(blockerIds))
            .willReturn(Map.of(BLOCKER_ID, Set.of(20L)));

        Map<Long, Set<Long>> result = sut.getBlockedMemberIdsBatch(blockerIds);

        assertThat(result).containsExactly(Map.entry(BLOCKER_ID, Set.of(20L)));
    }
}
