package com.umc.product.member.domain;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "member_block",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_member_block_blocker_blocked",
        columnNames = {"blocker_member_id", "blocked_member_id"}
    )
)
public class MemberBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_member_id", nullable = false)
    private Long blockerMemberId;

    @Column(name = "blocked_member_id", nullable = false)
    private Long blockedMemberId;

    public static MemberBlock create(Long blockerMemberId, Long blockedMemberId) {
        if (blockerMemberId == null || blockedMemberId == null) {
            throw new IllegalArgumentException("blockerMemberId and blockedMemberId must not be null");
        }
        if (blockerMemberId.equals(blockedMemberId)) {
            throw new IllegalArgumentException("blockerMemberId and blockedMemberId must differ");
        }
        return MemberBlock.builder()
            .blockerMemberId(blockerMemberId)
            .blockedMemberId(blockedMemberId)
            .build();
    }
}
