package com.umc.product.terms.adapter.out.persistence;

import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.application.port.out.SaveTermsPort;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TermsPersistenceAdapter implements LoadTermsPort, SaveTermsPort {

    private final TermsRepository repository;
    private final TermsQueryRepository queryRepository;

    @Override
    public Optional<Terms> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Terms> findActiveByType(TermsType type) {
        return queryRepository.findActiveByType(type);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public List<Terms> findAllActiveByTypes(List<TermsType> types) {
        return repository.findAllByTypeInAndActiveIsTrue(types);
    }

    @Override
    public Terms save(Terms terms) {
        return repository.save(terms);
    }

    @Override
    public void delete(Terms terms) {
        repository.delete(terms);
    }
}
