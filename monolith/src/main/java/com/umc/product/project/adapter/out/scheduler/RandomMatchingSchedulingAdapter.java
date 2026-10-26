package com.umc.product.project.adapter.out.scheduler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.out.ScheduleRandomMatchingPort;
import com.umc.product.project.domain.enums.MatchingType;

import lombok.RequiredArgsConstructor;

/**
 * 3차 공개 후 랜덤 매칭 예약 요청을 내부 이벤트로 전달한다.
 * 스케줄러는 공개 트랜잭션이 커밋된 뒤 별도 작업을 등록한다.
 */
@Component
@RequiredArgsConstructor
public class RandomMatchingSchedulingAdapter implements ScheduleRandomMatchingPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void scheduleAfterPublication(Long chapterId, MatchingType matchingType) {
        eventPublisher.publishEvent(new RandomMatchingScheduleRequested(chapterId, matchingType));
    }

    public record RandomMatchingScheduleRequested(Long chapterId, MatchingType matchingType) {
    }
}
