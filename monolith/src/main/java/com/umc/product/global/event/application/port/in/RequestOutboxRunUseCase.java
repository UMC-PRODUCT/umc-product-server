package com.umc.product.global.event.application.port.in;

public interface RequestOutboxRunUseCase {

    /**
     * Worker 실행을 요청한다. 수락 여부를 반환하며 이벤트 처리 완료를 기다리지 않는다.
     */
    boolean requestRun();
}
