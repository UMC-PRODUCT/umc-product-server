package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkbookSubmissionPersistenceAdapter implements LoadWorkbookSubmissionPort {

    private final WorkbookSubmissionQueryRepository workbookSubmissionQueryRepository;

    @Override
    public List<WorkbookSubmissionInfo> findSubmissions(GetWorkbookSubmissionsQuery query) {
        return workbookSubmissionQueryRepository.findSubmissions(query);
    }
}
