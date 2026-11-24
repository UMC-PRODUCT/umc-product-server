package com.umc.product.challenger.application.port.out;

public interface DeleteChallengerDeactivationPort {

    void deleteImagesByDeactivationId(Long deactivationId);

    void deleteLinksByDeactivationId(Long deactivationId);

    void deleteHistoriesByDeactivationId(Long deactivationId);

    void deleteById(Long deactivationId);
}
