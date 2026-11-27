package com.umc.product.notice.adapter.in.web.swagger;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.CreateNoticeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Notice | 공지사항 Command", description = "공지사항 생성, 수정, 삭제")
public interface NoticeCommandControllerApi {

    @Operation(
        operationId = "NOTICE-201",
        summary = "공지사항 생성",
        description = """
            제목은 필수이며 최대 255자, 본문은 필수이며 최대 3,000자입니다.
            길이를 초과하거나 비어 있으면 400(COMMON-400)을 반환하고 공지를 저장하지 않습니다.

            ### 작성 권한

            작성 권한은 대상 기수와 별개로 현재 활성 기수의 역할을 기준으로 확인합니다.
            교내 운영진은 본인 학교의 모든 파트에 작성할 수 있으며, 파트 미지정 학교 전체 공지는 회장·부회장만 작성할 수 있습니다.
            지부장은 운영진 공지를 작성할 수 없습니다. SUPER_ADMIN의 기존 작성 권한은 유지합니다.

            ### 알림 발송 조건

            - 작성 권한이 있는 공지는 운영진·전체 기수 공지를 포함하여 `shouldNotify=true`로 알림을 요청할 수 있습니다.
            - 운영진 공지는 대상 기수·학교·역할·담당 파트에 맞는 운영진에게만 알림을 요청합니다.
              지부장은 학교 미지정 운영진 공지의 수신 대상에만 포함됩니다.
            - 전체 기수 공지는 회원별 최신 챌린저 정보를 기준으로 대상을 계산하고, 같은 회원에게 중복 요청하지 않습니다.
            - 알림은 기존 비동기 발송 경로로 처리하며, 등록된 푸시 토큰이 있는 수신자에게 발송합니다.

            ---

            ## 운영진 공지 작성 방법

            `targetInfo.targetNoticeTab`을 `CHALLENGER` 이외의 값으로 설정하면 운영진 공지로 생성

            > **공통 제약**
            > - `targetGisuId` 는 항상 필수
            > - `targetChapterId` 지정 불가 (운영진 공지는 지부 단위 미지원)

            ### 중앙운영진 공지 (`targetSchoolId = null`)

            | 열람 대상 | targetNoticeTab | targetSchoolId | targetParts | 작성 권한 |
            |---|---|---|---|---|
            | 중앙운영진 전체 | `CENTRAL_MEMBER` | null | null | 총괄단 |
            | 중앙운영진 + 모든 학교회장단 | `SCHOOL_CORE` | null | null | 중앙운영진 |
            | 중앙운영진 + 모든 학교회장단 + 모든 파트장 | `SCHOOL_PART_LEADER` | null | null 또는 `[]` | 중앙운영진 |
            | 중앙운영진 + 모든 학교회장단 + 특정 파트 파트장 | `SCHOOL_PART_LEADER` | null | `["WEB_PRODUCT_ENGINEER"]` | 중앙운영진 |

            ### 교내운영진 공지 (`targetSchoolId` 지정 필수)

            | 열람 대상 | targetNoticeTab | targetSchoolId | targetParts | 작성 권한 |
            |---|---|---|---|---|
            | 해당 학교 파트장 전체 + 상위 운영진 | `SCHOOL_PART_LEADER` | schoolId | null 또는 `[]` | 해당 학교 회장단 |
            | 해당 학교 특정 파트 파트장 + 상위 운영진 | `SCHOOL_PART_LEADER` | schoolId | `["WEB_PRODUCT_ENGINEER"]` | 해당 학교 교내 운영진 |

            ### UPMS 공지 유형

            선택 필드 `upmsNoticeType`으로 UPMS 공지를 구분합니다.
            - `TEAM_MATCHING`: UPMS 팀 매칭 공지
            - `PROJECT_SETTING`: UPMS 프로젝트 설정 공지
            - 생략 또는 `null`: 일반 공지로 저장하며 기존 등록 처리를 유지합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "생성 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    CreateNoticeResponse createNotice(
        @RequestBody @Valid CreateNoticeRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );


    @Operation(
        operationId = "NOTICE-202",
        summary = "공지사항 삭제",
        description = "공지사항을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    void deleteNotice(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        operationId = "NOTICE-203",
        summary = "공지사항 수정",
        description = "공지사항 내용을 수정합니다. 제목과 본문을 모두 전달해야 하며, 제목은 최대 255자, 본문은 최대 3,000자입니다. "
            + "길이를 초과하거나 비어 있으면 400(COMMON-400)을 반환하고 기존 공지를 변경하지 않습니다. "
            + "mustRead=true로 설정하면 UPMS 필독 공지로 지정되어 목록 최상단에 고정되며, false로 변경하면 고정이 해제됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    void updateNotice(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "수정할 공지사항 정보", required = true)
        @RequestBody @Valid UpdateNoticeRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        operationId = "NOTICE-204",
        summary = "선택한 미확인자에게 재알림 발송",
        description = """
            챌린저 공지의 작성자와 SUPER_ADMIN이 재알림을 보낼 수 있습니다.
            수신 현황(GET /api/v1/notices/{noticeId}/read-status)에서 선택한 챌린저 ID를 targetIds로 전달하세요.
            하나 이상의 ID를 지정할 수 있으며, 요청 시점에 게시 대상이면서 공지를 읽지 않은 회원을 발송 대상으로 확정합니다.
            선택 발송은 횟수 제한 없이 가능하며 전체 발송의 일일 횟수에도 포함되지 않습니다.
            해당 회원이 없으면 400(NOTICE-0015)을 반환합니다. 운영진 공지에는 재알림을 보낼 수 없습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재알림 요청 처리 완료"),
        @ApiResponse(responseCode = "400", description = "운영진 공지, 미확인 대상 없음 또는 잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "재알림 발송 권한 없음"),
        @ApiResponse(responseCode = "404", description = "공지 또는 공지 대상을 찾을 수 없음")
    })
    void sendNoticeReminder(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid SendNoticeReminderRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        operationId = "NOTICE-205",
        summary = "공지사항 읽음 등록",
        description = "공지사항을 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "처리 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    void recordNoticeRead(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        operationId = "NOTICE-206",
        summary = "미확인자 전체에게 재알림 발송",
        description = """
            챌린저 공지의 작성자와 SUPER_ADMIN이 호출할 수 있습니다. 요청 본문은 없습니다.
            재알림 요청 시점에 원래 공지의 대상 조건을 충족하고 공지를 읽지 않은 회원 전원을 발송 대상으로 확정합니다.
            게시 후 대상에 합류한 회원도 포함하며, 대상에서 벗어난 회원은 제외합니다. 기존 공지에도 같은 기준을 적용합니다.
            전체 발송은 한국 시간 기준 공지당 하루 한 번입니다. 미확인 대상이 없으면 일일 횟수를 사용하지 않습니다.
            운영진 공지에는 재알림을 보낼 수 없습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전체 재알림 요청 처리 완료"),
        @ApiResponse(responseCode = "400", description = "운영진 공지 또는 미확인 대상 없음"),
        @ApiResponse(responseCode = "403", description = "재알림 발송 권한 없음"),
        @ApiResponse(responseCode = "404", description = "공지 또는 공지 대상을 찾을 수 없음"),
        @ApiResponse(responseCode = "429", description = "해당 공지의 당일 전체 재알림 횟수 초과")
    })
    void remindAllUnread(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );
}
