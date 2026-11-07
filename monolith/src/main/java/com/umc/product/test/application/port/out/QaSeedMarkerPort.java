package com.umc.product.test.application.port.out;

import java.util.Collection;
import java.util.Set;

/**
 * QA 시딩이 이미 만든 데이터를 {@code [QA]} 제목으로 찾는다. 제목 조회 Query UseCase가 없는 도메인에만 쓴다.
 */
public interface QaSeedMarkerPort {
    Set<String> findExistingNoticeTitles(Collection<String> titles);

    Set<String> findExistingScheduleNames(Collection<String> names);

    Set<String> findExistingThreadTitles(Collection<String> titles);
}
