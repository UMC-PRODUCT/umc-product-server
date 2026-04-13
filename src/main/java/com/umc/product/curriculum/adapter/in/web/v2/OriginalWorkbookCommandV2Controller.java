package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.global.exception.NotImplementedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums/original-workbooks")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | Original Workbook Command", description = "중앙운영사무국 교육국 소속 파트장용. 주차별 원본 워크북 생성/수정/삭제 등")
public class OriginalWorkbookCommandV2Controller {

    @Operation(
        summary = "중앙파트장용: 주차별 커리큘럼에 원본 워크북 추가",
        description = """
            주차별 시작 기간이 경과되지 않은 경우에만 추가가 가능합니다.
            미션 추가는 별도의 API를 이용해 주세요.
            """
    )
    public void createOriginalWorkbook() {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "중앙파트장용: 원본 워크북 수정",
        description = """
            원본 워크북의 제목 및 내용 등을 수정할 수 있습니다.
            따로 제한 없이 수정이 가능하며, 수정에 따른 책임은 중앙 파트장에게 있습니다.
            """
    )
    public void editOriginalWorkbook() {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "중앙파트장용: 원본 워크북 삭제",
        description = """
            배포받은 사용자가 존재하는 경우에는 삭제가 불가능합니다.
            """
    )
    public void deleteOriginalWorkbook() {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "중앙파트장용: 원본 워크북 배포 준비 완료 처리",
        description = """
            원본 워크북이 배포 준비가 된 상태, 즉 `READY` 상태로 변경합니다.
            READY 상태로 변경된 원본 워크북은 배포 시점이 되었을 때 (주차별 시작 시간 2주 전) 자동으로 스케쥴러에 의해서 배포될 수 있습니다.
            """
    )
    public void makeOriginalWorkbookReadyForRelease() {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "중앙파트장용: 원본 워크북 배포 처리",
        description = """
            원본 워크북을 배포 처리합니다.
            READY 상태에서만 배포 처리할 수 있습니다. (DRAFT, RELEASED 등 그 이외의 상태에서는 불가능)

            > `READY` `RELEASED`의 차이점은 아래와 같습니다.
            > - `READY`: 배포 시점이 되었을 때 (주차별 시작 시간 2주 전) 자동으로 스케쥴러에 의해서 배포되도 괜찮은 상태
            > - `RELEASED`: `READY` 상태에서 스케쥴러 등에 의해 자동으로 배포가 되었거나, 본 API에 의해서 수동으로 배포된 경우
            """
    )
    public void releaseOriginalWorkbook() {
        throw new NotImplementedException();
    }
}
