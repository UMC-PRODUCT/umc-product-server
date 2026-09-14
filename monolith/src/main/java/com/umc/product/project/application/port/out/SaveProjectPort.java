package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.Project;
import java.util.List;

/**
 * Project 쓰기 Port (Driven / Port Out).
 * <p>
 * 코드베이스 컨벤션: {@code save}, {@code saveAll}, {@code delete}는
 * 현재 사용 여부와 무관하게 함께 선언합니다.
 */
public interface SaveProjectPort {

    Project save(Project project);

    List<Project> saveAll(List<Project> projects);

    void delete(Project project);
}
