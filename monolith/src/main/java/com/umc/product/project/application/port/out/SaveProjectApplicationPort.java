package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectApplication;
import java.util.Collection;
import java.util.List;

public interface SaveProjectApplicationPort {

    ProjectApplication save(ProjectApplication application);

    /**
     * 자동 선발 시점에 다수 지원서의 status 를 일괄 갱신할 때 사용합니다.
     */
    List<ProjectApplication> saveAll(Collection<ProjectApplication> applications);
}
