package com.umc.product.form.application.port.out;

public interface LoadFormResponsePort {
    boolean existsByFormIdAndUserId(Long formId, Long userId);
}
