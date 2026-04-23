package com.umc.product.project.application.service.query;

import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryService implements
    GetProjectUseCase,
    SearchProjectUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;

    // Cross-domain
    private final GetFileUseCase getFileUseCase;

    @Override
    public ProjectInfo getById(Long projectId) {
        throw new UnsupportedOperationException("TODO: GetProjectUseCase.getById 구현 필요");
    }

    @Override
    public Optional<ProjectInfo> findDraftByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        throw new UnsupportedOperationException("TODO: GetProjectUseCase.findDraftByOwnerAndGisu 구현 필요");
    }

    @Override
    public Page<ProjectInfo> search(SearchProjectQuery query) {
        throw new UnsupportedOperationException("TODO: SearchProjectUseCase.search 구현 필요");
    }
}
