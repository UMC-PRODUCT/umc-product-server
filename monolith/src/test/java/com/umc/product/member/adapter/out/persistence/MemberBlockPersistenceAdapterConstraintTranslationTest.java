package com.umc.product.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.umc.product.member.domain.MemberBlock;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberBlockPersistenceAdapter 차단 중복 무결성 예외 변환")
class MemberBlockPersistenceAdapterConstraintTranslationTest {

    private static final String DUPLICATE_CONSTRAINT = "uq_member_block_blocker_blocked";

    @Mock
    MemberBlockJpaRepository memberBlockJpaRepository;

    MemberBlockPersistenceAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new MemberBlockPersistenceAdapter(memberBlockJpaRepository);
    }

    @Test
    @DisplayName("blocker/blocked 중복 unique 위반을 MEMBER_BLOCK_ALREADY_EXISTS로 변환한다")
    void save_중복_unique를_도메인_충돌로_변환한다() {
        DataIntegrityViolationException exception = duplicateViolation();
        given(memberBlockJpaRepository.saveAndFlush(any(MemberBlock.class))).willThrow(exception);

        assertThatThrownBy(() -> sut.save(MemberBlock.create(1L, 2L)))
            .isInstanceOfSatisfying(MemberDomainException.class, domainException -> {
                assertThat(domainException.getBaseCode()).isEqualTo(MemberErrorCode.MEMBER_BLOCK_ALREADY_EXISTS);
                assertThat(domainException.getCause()).isSameAs(exception);
            });
    }

    @Test
    @DisplayName("다른 무결성 위반은 원래 DataIntegrityViolationException을 그대로 전달한다")
    void save_다른_무결성_위반은_그대로_전달한다() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "duplicate key value violates unique constraint \"other_constraint\""
        );
        given(memberBlockJpaRepository.saveAndFlush(any(MemberBlock.class))).willThrow(exception);

        assertThatThrownBy(() -> sut.save(MemberBlock.create(1L, 2L)))
            .isSameAs(exception);
    }

    private DataIntegrityViolationException duplicateViolation() {
        ConstraintViolationException constraintViolation = new ConstraintViolationException(
            "duplicate key",
            new SQLException("duplicate key"),
            DUPLICATE_CONSTRAINT
        );
        return new DataIntegrityViolationException("member_block insert failed", constraintViolation);
    }
}
