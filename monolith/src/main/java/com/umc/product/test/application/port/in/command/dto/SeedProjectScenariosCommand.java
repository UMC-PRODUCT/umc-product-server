package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * 시나리오 시딩 Command.
 * <p>
 * 활성 기수에 대해 {@link TargetProjectStatus} 까지 도달한 프로젝트를 {@code projectCount} 개 생성한다.
 * <p>
 * {@code productOwnerMemberIds} 가 null 이면 활성 기수의 PLAN 챌린저 풀에서 무작위로 N 명을 PO 로
 * 선정한다. 명시되면 그 리스트의 size 가 정확히 {@code projectCount} 와 같아야 하고, 각 멤버는
 * 활성 기수의 PLAN 챌린저여야 한다(시딩 측에서 챌린저를 강제로 만들지 않는다).
 *
 * @param targetStatus           시딩 시 도달할 프로젝트 상태
 * @param projectCount           생성할 프로젝트 수
 * @param productOwnerMemberIds  PO 로 사용할 멤버 ID 목록. null 이면 랜덤 픽
 */
public record SeedProjectScenariosCommand(
    TargetProjectStatus targetStatus,
    int projectCount,
    List<Long> productOwnerMemberIds
) {
}
