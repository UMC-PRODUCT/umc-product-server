package com.umc.product.test.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.umc.product.test.domain.rehearsal.RoundSnapshot;
import com.umc.product.test.domain.startingline.OwnedProject;

/**
 * 리허설 판단에 쓰는 프로젝트 사실을 DB에서 직접 읽는다. 운영 조회 화면과 같은 코드 경로를 쓰지 않아,
 * 그 화면들을 확인하는 기준값이 된다.
 */
public interface SeedProjectReadPort {

    /**
     * PO들이 그 기수에 가진 DRAFT·승인 대기·진행 중 프로젝트. PO마다 가장 먼저 만든 하나만 돌려준다.
     */
    List<OwnedProject> listOwnedProjects(Long gisuId, Collection<Long> ownerMemberIds);

    Optional<RoundSnapshot> findRoundSnapshot(Long roundId);
}
