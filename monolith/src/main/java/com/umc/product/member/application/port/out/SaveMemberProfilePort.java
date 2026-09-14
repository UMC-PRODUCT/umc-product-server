package com.umc.product.member.application.port.out;

import java.util.List;

import com.umc.product.member.domain.MemberProfile;

public interface SaveMemberProfilePort {

    MemberProfile save(MemberProfile memberProfile);

    List<MemberProfile> saveAll(List<MemberProfile> memberProfiles);

    void delete(MemberProfile memberProfile);
}
