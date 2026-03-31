package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.curriculum.adapter.in.web.v2.swagger.CurriculumV2ControllerApi;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums/workbooks")
@RequiredArgsConstructor
public class CurriculumV2Controller implements CurriculumV2ControllerApi {

    @Override
    @PutMapping("/{challengerWorkbookId}/submission")
    public void updateSubmission(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId
    ) {
        // TODO: UseCase 연결
    }
}
