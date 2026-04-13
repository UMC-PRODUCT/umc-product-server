package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.curriculum.adapter.in.web.v2.dto.request.CreateCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.CreateWeeklyCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.EditWeeklyCurriculumRequest;
import com.umc.product.global.exception.NotImplementedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | Curriculum & WeeklyCurriculum Command", description = "중앙운영사무국 교육국 소속 파트장용. 커리큘럼 및 그 주차별 내용 생성, 수정, 삭제 등")
public class CurriculumCommandV2Controller {

    // ==== Curriculum CUD ====

    @Operation(
        summary = "커리큘럼 생성",
        description = """
            기수, 파트에 대한 상위 객체인 커리큘럼을 생성합니다.

            단, 동일한 기수에 동일한 파트에 대한 커리큘럼은 존재할 수 없습니다.
            """
    )
    @PostMapping
    public void createCurriculum(
        @RequestBody CreateCurriculumRequest request
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "커리큘럼 수정",
        description = """
            상위 객체인 커리큘럼을 수정합니다.

            기수, 파트 등은 수정이 불가능하며 커리큘럼의 이름만 수정 가능합니다.
            """
    )
    @PatchMapping("/{curriculumId}")
    public void editCurriculum(
        @RequestBody String title,
        @PathVariable String curriculumId
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "중앙운영사무국 총괄단용: 커리큘럼 삭제",
        description = """
            - 커리큘럼 내부에 포함된 주차별 커리큘럼이 존재하는 경우 삭제하지 못합니다.
            - 중앙운영사무국 총괄단 이상의 권한을 보유한 경우에만 삭제가 가능합니다.
            """
    )
    @DeleteMapping("/{curriculumId}")
    public void deleteCurriculum(
        @PathVariable String curriculumId
    ) {
        throw new NotImplementedException();
    }

    // ==== Weekly Curriculum CUD ====

    @Operation(
        summary = "각 커리큘럼에 새로운 주차 생성",
        description = """
            상위 객체인 커리큘럼에 각 주차별 커리큘럼을 생성합니다.

            각 커리큘럼에 대해서, 주차별 커리큘럼은 최대 2개 (MAIN, EXTRA) 생성 가능합니다.
            주차별 커리큘럼 내에 원본 워크북 수와는 다른 개념입니다!
            """
    )
    @PostMapping("/weekly")
    public void createWeeklyCurriculum(
        @RequestBody CreateWeeklyCurriculumRequest request
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "주차별 커리큘럼 수정",
        description = """
            주차별 커리큘럼 자체를 수정합니다.

            - 제목은 상시로 수정이 가능합니다.
            - 포홤된 주차별 워크북이 하나라도 배포된 상태라면 시작/종료일은 수정이 불가능합니다.
            - 시작일 < 종료일
            - 주차는 이미 설정되지 않은 주차로만 수정이 가능합니다.
            - UK 제약을 어기는 수정은 불허합니다. (e.g. 정규, 부록 -> 정규, 정규 불가능)
            """
    )
    @PatchMapping("/weekly/{weeklyCurriculumId}")
    public void editWeeklyCurriculum(
        @PathVariable String weeklyCurriculumId,
        @RequestBody EditWeeklyCurriculumRequest request
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "주차별 커리큘럼 삭제",
        description = """
            주차별 커리큘럼을 삭제합니다.

            - 포함된 주차별 워크북이 하나라도 존재하면 삭제가 불가능합니다.
            """
    )
    @DeleteMapping("/weekly/{weeklyCurriculumId}")
    public void deleteWeeklyCurriculum(
        @PathVariable String weeklyCurriculumId
    ) {
        throw new NotImplementedException();
    }
}
