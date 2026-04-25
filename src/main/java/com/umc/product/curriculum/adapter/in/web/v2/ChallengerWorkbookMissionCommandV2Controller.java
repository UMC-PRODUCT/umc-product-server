package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.curriculum.adapter.in.web.v2.dto.request.CreateMissionFeedbackRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.CreateMissionSubmissionRequest;
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
@RequestMapping("/api/v2/curriculums/challenger-workbooks/missions")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | Challenger Workbook Mission Command", description = "챌린저/파트장용 - 워크북 미션 및 피드백 제출/수정/철회 등")
public class ChallengerWorkbookMissionCommandV2Controller {

    // TODO: @CheckAccess 반드시 추가할 것

    @Operation(
        summary = "챌린저용: 워크북 내 미션 제출",
        description = """
            미션을 제출합니다.

            #### 소속된 스터디 그룹에 주차별 일정이 등록된 경우의 동작
            - KST 기준 일정 당일 00:00 이전: 제출 가능
            - 그 이후 ~ 주차별 종료 일자 이전: 제출은 가능하나, LATE 처리되어 벌점 부과 대상임 (내부적으로는 제출 시각만을 기록하고, 그를 기준으로 벌점 부과 처리)
            - 주차별 종료 일자 이후: 제출 불가능

            #### 주차별 일정이 등록되지 않은 경우의 동작
            - 주차별 종료 일자 이전: 제출 가능
            - 그 이후: 제출 불가능
            - 이후 스터디 일정이 등록된 순간, KST 기준 해당 일정 시작일 00:00 이후에 제출된 미션은 모두 LATE 처리되어 벌점이 부과됩니다.

            > p.s. 미션의 LATE 처리는 createdAt이 아닌 updatedAt을 기준으로 합니다.
            """
    )
    @PostMapping
    public void createOriginalWorkbookMission(
        @RequestBody CreateMissionSubmissionRequest request
    ) {
        // TODO: 스케쥴러를 매일 KST 기준 00:00 (또는 다른 시간) 에 돌려서, LATE 처리된 미션에 대해서 벌점을 부과할 필요가 있습니다. 단,중복 벌점 부과는 없도록 유의해야 합니다.

        throw new NotImplementedException();
    }


    @Operation(
        summary = "챌린저용: 제출한 워크북 미션 수정",
        description = """
            이미 제출된 미션의 내용을 수정합니다.

            #### 소속된 스터디 그룹에 주차별 일정이 등록된 경우의 동작
            - KST 기준 일정 당일 00:00 이전: 수정 가능
            - 그 이후: 불가능

            #### 주차별 일정이 등록되지 않은 경우의 동작
            - 주차별 종료 일자 이전: 수정 가능
            - 그 이후: 불가능
            - 주차별 일정이 등록되는 순간, updatedAt을 기준으로 해당 일정 시작일 00:00 이후에 수정된 미션은 모두 LATE 처리되어 벌점이 부과됩니다.
            """
    )
    @PatchMapping("/{missionSubmissionId}")
    public void editOriginalMission(
        @PathVariable Long missionSubmissionId,
        @RequestBody String content
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "챌린저용: 제출한 워크북 미션 철회",
        description = """
            이미 제출한 미션을 철회합니다.

            기간과 관계없이 철회가 가능하나, 그 후 재제출이 불가능해 LATE 처리가 되어 벌점이 부과될 수 있는 부분은 삭제한 사람에게 책임이 있습니다.
            """
    )
    @DeleteMapping("/{missionSubmissionId}")
    public void deleteOriginalMission(
        @PathVariable Long missionSubmissionId
    ) {
        throw new NotImplementedException();
    }

    // ===== 운영진용 ====

    @Operation(
        summary = "운영진용: 제출된 미션에 대한 피드백 작성",
        description = """
            챌린저가 제출한 미션에 대한 피드백을 작성합니다.

            - 스터디 그룹의 일정이 등록된 경우에만, 해당 일자의 00:00 이후에 피드백 작성이 가능합니다.
            - 피드백은 해당 기수에 챌린저로 활동하는 모든 사람이 조회할 수 있습니다.
            - N주차 워크북에 대한 피드백은 N+1주차의 수요일 00:00 이전까지 제공되어야 합니다.
                - 해당 일자까지 피드백이 작성되지 않은 경우 해당 스터디 그룹의 파트장에게 벌점이 부과됩니다.
                - 파트장에 대한 벌점은 주차별로 한 번만 부과할 수 있습니다. (e.g. N개의 스터디 그룹에 대한 파트장이여도, 주차별로 1번만 벌점을 부과할 수 있습니다)
                - 스터디 그룹을 관리하는 파트장 중 한 명만 피드백을 작성하면 됩니다.
                - 챌린저 미션과는 다르게, 피드백 최초 작성 시점을 기준으로 벌점을 부과합니다.
            - 단, 피드백이라는 특성을 고려하여 시간과 관계없이 작성은 가능합니다.
            - 선택인 미션에 대한 피드백은 필수적이지 않습니다.
                - 선택 미션에 대한 피드백은 기간이 경과된 이후에 작성하여도 불이익이 존재하지 않습니다.
            """
    )
    @PostMapping("/feedback")
    public void createMissionFeedback(
        @RequestBody CreateMissionFeedbackRequest request
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "운영진용: 제출된 미션에 대한 피드백 수정",
        description = """
            챌린저에게 제공된 피드백을 수정합니다.

            - 작성일 기준 2주가 경과되기 전까지만 수정이 가능합니다.
            - PASS->FAIL 처리는 불가능합니다.
            - 피드백 최초 작성 일자 기준으로 벌점이 부과되기 때문에 수정은 벌점 부과와는 대부분의 경우에서 무관합니다.
            """
    )
    @PatchMapping("/feedback/{missionFeedbackId}")
    public void editMissionFeedback(
        @PathVariable Long missionFeedbackId,
        @RequestBody String content
    ) {
        throw new NotImplementedException();
    }


    @Operation(
        summary = "운영진용: 제출된 미션에 대한 피드백 삭제",
        description = """
            챌린저에게 제공된 피드백을 삭제합니다.
            삭제로 인한 벌점 부과 등의 책임은 삭제한 본인에게 있습니다.

            해당 기수 종료 이후에는 피드백 삭제가 불가능합니다.
            """
    )
    @DeleteMapping("/feedback/{missionFeedbackId}")
    public void deleteMissionFeedback(
        @PathVariable Long missionFeedbackId
    ) {
        throw new NotImplementedException();
    }

}
