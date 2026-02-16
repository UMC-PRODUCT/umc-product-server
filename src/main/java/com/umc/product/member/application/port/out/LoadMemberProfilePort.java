package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.MemberProfile;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadMemberProfilePort {

    Optional<MemberProfile> findById(Long id);

    List<MemberProfile> findByIdIn(Set<Long> ids);
}