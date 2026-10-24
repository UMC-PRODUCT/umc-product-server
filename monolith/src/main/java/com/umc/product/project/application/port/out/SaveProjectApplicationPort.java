package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;

import com.umc.product.project.domain.ProjectApplication;

public interface SaveProjectApplicationPort {

    /** 프로젝트 삭제 시 해당 프로젝트의 지원서를 일괄 삭제한다. */
    void deleteAllByProjectId(Long projectId);

    ProjectApplication save(ProjectApplication application);

    /**
     * 자동 선발 시점에 다수 지원서의 status 를 일괄 갱신할 때 사용합니다.
     */
    List<ProjectApplication> saveAll(Collection<ProjectApplication> applications);
}
