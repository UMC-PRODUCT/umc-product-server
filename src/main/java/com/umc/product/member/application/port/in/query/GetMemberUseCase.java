package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GetMemberUseCase {
    MemberInfo getById(Long memberId);

    Optional<MemberInfo> findById(Long memberId);

    /**
     * 멤버 ID로 멤버 정보를 조회합니다. 멤버가 존재하지 않을 경우 null을 반환합니다.
     *
     * @deprecated {@link #findById}를 사용해주세요.
     */
    @Deprecated(since = "v1.5.0", forRemoval = true)
    MemberInfo findByIdOrNull(Long memberId);

    Map<Long, MemberInfo> findAllByIds(Set<Long> memberIds);

    Map<Long, Long> findAllSchoolIdsByIds(Set<Long> memberIds);

    boolean existsById(Long memberId);

    boolean existsByEmail(String email);
}
