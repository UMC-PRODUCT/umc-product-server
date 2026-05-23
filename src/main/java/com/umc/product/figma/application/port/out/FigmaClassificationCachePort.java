package com.umc.product.figma.application.port.out;

import java.util.Optional;

public interface FigmaClassificationCachePort {

    Optional<String> get(String commentId);

    boolean contains(String commentId);

    void put(String commentId, Optional<String> domainKey);
}
