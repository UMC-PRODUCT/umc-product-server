package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplicationForm;
import java.util.List;

/**
 * ProjectApplicationForm 쓰기 Port (Driven / Port Out).
 * <p>
 * 코드베이스 컨벤션: {@code save}, {@code saveAll}, {@code delete}는
 * 현재 사용 여부와 무관하게 함께 선언합니다.
 */
public interface SaveProjectApplicationFormPort {

    ProjectApplicationForm save(ProjectApplicationForm form);

    List<ProjectApplicationForm> saveAll(List<ProjectApplicationForm> forms);

    void delete(ProjectApplicationForm form);
}
