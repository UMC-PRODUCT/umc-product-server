package com.umc.product.authentication.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.umc.product.authentication.application.port.out.DeleteRefreshTokenPort;
import com.umc.product.authentication.application.port.out.LoadRefreshTokenPort;
import com.umc.product.authentication.application.port.out.SaveRefreshTokenPort;
import com.umc.product.authentication.domain.RefreshToken;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements LoadRefreshTokenPort, SaveRefreshTokenPort,
    DeleteRefreshTokenPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public Optional<RefreshToken> findByJti(UUID jti) {
        return refreshTokenJpaRepository.findByJti(jti);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(refreshToken);
    }

    @Override
    public boolean deleteByJti(UUID jti) {
        return refreshTokenJpaRepository.deleteByJti(jti) > 0;
    }
}
