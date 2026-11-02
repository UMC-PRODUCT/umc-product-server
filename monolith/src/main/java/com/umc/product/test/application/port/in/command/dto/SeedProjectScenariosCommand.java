package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * 시나리오 시딩 Command.
 * <p>
 * 활성 기수에 대해 {@link TargetProjectStatus} 까지 도달한 프로젝트를 {@code projectCount} 개 생성한다.
 * <p>
 * {@code DRAFT} / {@code PENDING_REVIEW} 는 PO 본인이 작성·제출한다. {@code IN_PROGRESS} 는 운영진
 * ({@code adminMemberId}, 해당 기수 총괄단 또는 지부장)이 PO 를 지정해 직접 등록하고 TO 를 정해 공개한다.
 * <p>
 * {@code productOwnerMemberIds} 가 null 이면 활성 기수의 PLAN 챌린저 중에서 무작위로 N 명을 PO 로 선정한다.
 * {@code IN_PROGRESS} 는 운영진이 관리하는 지부의 PLAN 챌린저로 후보를 좁힌다. 명시되면 그 리스트의 size 가
 * 정확히 {@code projectCount} 와 같아야 하고, 각 멤버는 활성 기수의 PLAN 챌린저여야 한다.
 *
 * @param targetStatus          시딩 시 도달할 프로젝트 상태
 * @param projectCount          생성할 프로젝트 수
 * @param productOwnerMemberIds PO 로 사용할 멤버 ID 목록. null 이면 랜덤 픽
 * @param adminMemberId         {@code IN_PROGRESS} 를 등록·공개할 운영진 멤버 ID. 다른 상태에서는 쓰지 않는다
 */
public record SeedProjectScenariosCommand(
    TargetProjectStatus targetStatus,
    int projectCount,
    List<Long> productOwnerMemberIds,
    Long adminMemberId
) {

    public SeedProjectScenariosCommand {
        if (targetStatus == TargetProjectStatus.IN_PROGRESS && adminMemberId == null) {
            throw new IllegalArgumentException("adminMemberId is required when targetStatus is IN_PROGRESS");
        }
    }
}
