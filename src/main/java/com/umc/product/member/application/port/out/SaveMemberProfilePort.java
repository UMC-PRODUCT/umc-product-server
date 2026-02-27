package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.MemberProfile;
import java.util.List;

public interface SaveMemberProfilePort {

    MemberProfile save(MemberProfile memberProfile);

    List<MemberProfile> saveAll(List<MemberProfile> memberProfiles);

    void delete(MemberProfile memberProfile);
}