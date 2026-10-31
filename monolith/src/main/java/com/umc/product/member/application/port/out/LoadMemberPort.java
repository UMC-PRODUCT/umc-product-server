package com.umc.product.member.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.umc.product.member.domain.Member;

public interface LoadMemberPort {
    Optional<Member> findById(Long id);

    Optional<Member> findByIdForUpdate(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    List<Member> findAllByIds(Set<Long> ids);

    Set<Long> searchIdsByName(Set<Long> candidateMemberIds, String name);

    Set<Long> listIdsBySchoolId(Long schoolId);

    Map<Long, Set<Long>> listIdsBySchoolIds(Set<Long> schoolIds);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    /**
     * 학교 내 모든 저장 회원을 대상으로 닉네임 존재 여부를 확인한다. 회원 상태와 기수는 제한하지 않는다.
     */
    boolean existsNicknameInSchool(Long schoolId, String nickname);

    List<Long> findAllIdsCursor(Long lastId, Pageable pageable);

    long countMembersByIds(Set<Long> memberIds);

    long countAllMembers();
}
