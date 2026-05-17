package com.umc.product.global.event.adapter.out;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.domain.DomainEvent;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring {@link ApplicationEventPublisher}로 위임하는 기본 어댑터.
 * <p>
 * 발행된 이벤트는 Spring 컨테이너 내부에서 디스패치되므로, 동일 JVM 내 리스너만 수신할 수 있다.
 * 외부 브로커(Kafka, RabbitMQ 등) 도입 시점에는 동일 포트를 구현하는 다른 어댑터로 교체한다.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }

    @Override
    public void publishAll(Collection<? extends DomainEvent> events) {
        events.forEach(delegate::publishEvent);
    }
}
