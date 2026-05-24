package com.umc.product.chat.adapter.out.persistence;

import com.umc.product.chat.domain.ChatMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberJpaRepository extends JpaRepository<ChatMember, Long> {

    List<ChatMember> findAllByRoomId(Long roomId);

    List<ChatMember> findAllByMemberId(Long memberId);

    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);

    void deleteByRoomIdAndMemberId(Long roomId, Long memberId);
}
