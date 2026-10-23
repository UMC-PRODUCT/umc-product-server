package com.umc.product.authentication.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.authentication.domain.SsoSocialLoginAttempt;

import jakarta.persistence.LockModeType;

public interface SsoSocialLoginAttemptJpaRepository extends JpaRepository<SsoSocialLoginAttempt, Long> {
    // 같은 state의 callback이 동시에 도착해도 한 요청만 소비하도록 행 단위 쓰기 잠금을 잡는다.
    // 메서드 이름에 ForUpdate가 없어도 이 @Lock이 실제 SELECT의 잠금 방식을 결정한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SsoSocialLoginAttempt> findByStateHash(String stateHash);

    // 사용 여부와 무관하게 이미 만료된 시도는 재사용할 수 없으므로 한 번의 bulk DELETE로 정리한다.
    @Modifying
    @Query("delete from SsoSocialLoginAttempt a where a.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
