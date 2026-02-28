package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerRecordResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.request.AddChallengerRecordToMemberRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerRecordRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerRecordResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notification.application.port.in.annotation.WebhookAlarm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger-record")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 기록", description = "챌린저 기록 관련")
public class ChallengerRecordController {

    private final ChallengerRecordResponseAssembler assembler;
    private final ManageChallengerRecordUseCase manageChallengerRecordUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;

    // 코드를 이용해서 Member에 챌린저 기록을 추가하는 API
    @Operation(summary = "6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가",
        description = """
            각 챌린저 활동 기록에 대해서 발급된 6자리 코드를 입력하여,
            현재 로그인한 계정에 챌린저 기록 및 권한을 추가하는 기능입니다.

            각 코드는 1회만 생성 가능하며, 어떤 계정에, 언제 사용되었는지 기록됩니다.
            """)
    @PostMapping("member")
    @WebhookAlarm(
        title = "'챌린저 기록이 추가되었어요!'",
        content = "'회원 ID: ' + #memberPrincipal.getMemberId() + '\n챌린저 코드: ' + #request.code"
    )
    public void addChallengerRecordToMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody AddChallengerRecordToMemberRequest request) {

        manageChallengerUseCase.createWithRecord(memberPrincipal.getMemberId(), request.code());
    }

    @GetMapping("code/{code}")
    @Operation(summary = "코드로 ChallengerRecord 조회")
    public ChallengerRecordResponse getChallengerRecordByCode(
        @PathVariable String code
    ) {
        return assembler.from(code);
    }

    @GetMapping("id/{id}")
    @Operation(summary = "ID로 ChallengerRecord 조회")
    public ChallengerRecordResponse getChallengerRecordById(
        @PathVariable Long id
    ) {
        return assembler.from(id);
    }

    // 코드를 생성하는 API
    @Operation(summary = "[ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능",
        description = """
            중앙운영사무국 총괄단만 사용 가능한 기능입니다. 9기 이전 기수의 챌린저 기록을 업로드하고,
            각 기록을 모든 회원이 추가할 수 있도록 6자리 코드를 생성하여 발급합니다.
            """)
    @PostMapping
    public ChallengerRecordResponse createChallengerRecord(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody CreateChallengerRecordRequest request
    ) {
        // TODO: SUPER_ADMIN 만 가능하도록 권한 설정

        Long id = manageChallengerRecordUseCase.create(
            CreateChallengerRecordCommand.builder()
                .part(request.part())
                .creatorMemberId(memberPrincipal.getMemberId())
                .gisuId(request.gisuId())
                .chapterId(request.chapterId())
                .schoolId(request.schoolId())
                .memberName(request.memberName())
                .build()
        );

        return assembler.from(id);
    }

    @Operation(summary = "[ADMIN] 챌린저 기록용 코드 추가",
        description = """
            Response는 생성된 챌린저 기록의 ID 리스트입니다. (성능 상 이슈로 각각에 대해서는 id 및 code로 조회하는 API 이용)

            중앙운영사무국 총괄단만 사용 가능한 기능입니다. 9기 이전 기수의 챌린저 기록을 업로드하고,
            각 기록을 모든 회원이 추가할 수 있도록 6자리 코드를 생성하여 발급합니다.
            """)
    @PostMapping("bulk")
    public List<Long> createChallengerRecordBulk(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody List<CreateChallengerRecordRequest> request
    ) {
        List<Long> ids = manageChallengerRecordUseCase.createBulk(
            request.stream()
                .map(req -> CreateChallengerRecordCommand.builder()
                    .part(req.part())
                    .creatorMemberId(memberPrincipal.getMemberId())
                    .gisuId(req.gisuId())
                    .chapterId(req.chapterId())
                    .schoolId(req.schoolId())
                    .memberName(req.memberName())
                    .build())
                .toList()
        );

        return ids;
    }

}
