package com.umc.product.github.application.port.in;

import com.umc.product.github.application.port.in.dto.GithubWebhookCommand;

/**
 * 서명 검증을 통과한 GitHub 웹훅을 받아 활동 메트릭으로 집계하는 UseCase.
 */
public interface HandleGithubWebhookUseCase {

    /**
     * 웹훅 1건을 처리한다. 구현체는 비동기로 동작하므로 호출자(컨트롤러)는 즉시 200 을 반환할 수 있다.
     */
    void handle(GithubWebhookCommand command);
}
