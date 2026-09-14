package com.umc.product.member.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.out.LoadMemberProfilePort;
import com.umc.product.member.application.port.out.SaveMemberProfilePort;
import com.umc.product.member.domain.MemberProfile;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberProfilePersistenceAdapter implements LoadMemberProfilePort, SaveMemberProfilePort {

    private final MemberProfileJpaRepository repository;

    @Override
    public Optional<MemberProfile> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<MemberProfile> findByIdIn(Set<Long> ids) {
        return repository.findAllById(ids);
    }

    @Override
    public MemberProfile save(MemberProfile memberProfile) {
        return repository.save(memberProfile);
    }

    @Override
    public List<MemberProfile> saveAll(List<MemberProfile> memberProfiles) {
        return repository.saveAll(memberProfiles);
    }

    @Override
    public void delete(MemberProfile memberProfile) {
        repository.delete(memberProfile);
    }
}
