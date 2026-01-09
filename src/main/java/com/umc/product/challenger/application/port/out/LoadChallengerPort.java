package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.Challenger;
import java.util.List;
import java.util.Optional;

public interface LoadChallengerPort {

    Optional<Challenger> findById(Long id);

    List<Challenger> findByGisuId(Long gisuId);
}
