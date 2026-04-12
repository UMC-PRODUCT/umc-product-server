package com.umc.product.survey.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/surveys/votes")
@RequiredArgsConstructor
@Tag(name = "Survey | 설문", description = "")
public class VoteController {

//    private final CreateVoteUseCase createVoteUseCase;
//    private final DeleteVoteUseCase deleteVoteUseCase;
//    private final SubmitVoteResponseUseCase submitVoteResponseUseCase;
//    private final UpdateVoteResponseUseCase updateVoteResponseUseCase;
//    private final GetVoteDetailUseCase getVoteDetailUseCase;

//    @PostMapping
//    @Operation(
//        summary = "투표 생성",
//        description = """
//            공지사항에 연동될 투표(Form)를 생성합니다.
//
//            - 투표는 내부적으로 form_section 1개 / question 1개 구조로 생성됩니다.
//            - option(선택지)은 2개 이상 5개 이하만 허용됩니다.
//            - 시작일은 오늘(KST 기준)부터 선택 가능합니다.
//            - 마감일은 시작일의 다음 날부터 선택 가능합니다.
//            - 시작일은 00:00(KST), 마감일은 선택한 날짜의 23:59까지 유효합니다. (서버에서는 요청으로 넘겨준 마감일+1의 00:00 기준으로 exclusive 하게 저장합니다.)
//            - questionType: 단일 선택은 RADIO, 복수 선택은 CHECKBOX로 생성됩니다.
//            - 투표 수정 API는 제공하지 않으며, 공지 발행 전 프론트에서 관리 후 최종 생성합니다.
//            """
//    )
//    public CreateVoteResponse createVote(
//        @RequestBody CreateVoteRequest request,
//        @CurrentMember MemberPrincipal memberPrincipal
//    ) {
//        Long createdMemberId = memberPrincipal.getMemberId();
//
//        CreateVoteCommand cmd = request.toCommand(createdMemberId);
//        Long voteId = createVoteUseCase.create(cmd);
//
//        return new CreateVoteResponse(voteId);
//    }
//
//    @DeleteMapping("/{voteId}")
//    @Operation(
//        summary = "투표 삭제",
//        description = """
//            특정 투표(Form)를 삭제합니다.
//
//            - 이미 응답이 존재하는 투표도 삭제 가능합니다.
//            - 삭제 시 해당 투표의 응답(FormResponse) 및 답변(SingleAnswer)도 함께 삭제됩니다.
//            - 권한 검증은 추후 추가 예정입니다.
//            """
//    )
//    public void delete(
//        @PathVariable Long voteId,
//        @CurrentMember MemberPrincipal memberPrincipal
//    ) {
//        DeleteVoteCommand command = new DeleteVoteCommand(voteId, memberPrincipal.getMemberId());
//        deleteVoteUseCase.delete(command);
//    }
//
//    @PostMapping("/{voteId}/responses")
//    @Operation(
//        summary = "투표 응답",
//        description = """
//            특정 투표에 대해 사용자의 응답을 제출합니다.
//
//            - 투표 기간 내(OPEN 상태)에서만 응답 가능합니다.
//            - 한 사용자당 1회만 응답 가능합니다.
//            - 단일 선택(RADIO)의 경우 1개만 선택해야 합니다.
//            - 복수 선택(CHECKBOX)의 경우 여러 개 선택 가능합니다.
//            - 선택한 optionId는 해당 투표의 옵션에 포함되어 있어야 합니다.
//            """
//    )
//    public void submit(
//        @PathVariable Long voteId,
//        @RequestBody SubmitVoteResponseRequest request,
//        @CurrentMember MemberPrincipal memberPrincipal
//    ) {
//        SubmitVoteResponseCommand command = request.toCommand(voteId, memberPrincipal.getMemberId());
//        submitVoteResponseUseCase.submit(command);
//    }
//
//    @PutMapping("/{voteId}/responses")
//    @Operation(
//        summary = "투표 응답 수정",
//        description = """
//        특정 투표에 대해 사용자의 기존 응답을 수정합니다.
//
//        - 투표 기간 내(OPEN 상태)에서만 수정 가능합니다.
//        - 기존에 제출한 응답이 있어야 수정 가능합니다.
//        - 단일 선택(RADIO)의 경우 1개만 선택해야 합니다.
//        - 복수 선택(CHECKBOX)의 경우 여러 개 선택 가능합니다.
//        - 선택한 optionId는 해당 투표의 옵션에 포함되어 있어야 합니다.
//        """
//    )
//    public void update(
//        @PathVariable Long voteId,
//        @RequestBody SubmitVoteResponseRequest request,
//        @CurrentMember MemberPrincipal memberPrincipal
//    ) {
//        UpdateVoteResponseCommand command =
//            request.toUpdateCommand(voteId, memberPrincipal.getMemberId());
//        updateVoteResponseUseCase.update(command);
//    }
//
//    @Deprecated
//    @GetMapping("/{voteId}")
//    @Operation(
//        summary = "투표 상세 조회",
//        description = """
//            usecase 동작 확인을 위한 테스트용 API로, 실제 연동에는 사용되지 않습니다.
//
//            특정 투표의 상세 정보를 조회합니다.
//
//            반환 정보:
//            - 투표 기본 정보 (제목, 익명 여부, 단일/다중 선택 여부)
//            - 투표 상태 (NOT_STARTED / OPEN / CLOSED)
//            - 시작일/마감일 (KST 기준 날짜)
//            - 총 참여자 수
//            - 옵션별 득표 수 및 득표율(%, 소수점 1자리 까지 제공)
//            - 현재 사용자의 선택 정보 (미투표 시 빈 배열)
//
//            """
//    )
//    public VoteInfo getVoteDetail(
//        @PathVariable Long voteId,
//        @CurrentMember MemberPrincipal memberPrincipal
//    ) {
//        return getVoteDetailUseCase.get(
//            new GetVoteDetailsQuery(voteId, memberPrincipal.getMemberId())
//        );
//    }
}
