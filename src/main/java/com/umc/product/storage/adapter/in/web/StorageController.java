package com.umc.product.storage.adapter.in.web;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.storage.adapter.in.web.dto.PrepareUploadRequest;
import com.umc.product.storage.adapter.in.web.dto.PrepareUploadResponse;
import com.umc.product.storage.application.port.in.command.ManageFileUseCase;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "Storage | 파일 업로드 관련", description = "")
public class StorageController {

    private final ManageFileUseCase manageFileUseCase;
    private final GetFileUseCase getFileUseCase;

    /**
     * 파일 업로드를 위한 Signed URL을 생성합니다.
     */
    @PostMapping("/prepare-upload")
    @Operation(summary = "파일 업로드를 위한 Signed URL을 생성합니다.", description = """
        파일 카테고리는 Schema를 참고하세요. (추후 변경 가능)

        업로드 완료 후 반드시 "파일 업로드 완료" API를 호출해야 정상적으로 파일 등록이 완료됩니다.

        Content-type는 MIME 타입을 정확히 기재하여야 하며, fileName은 확장자를 포함하여야 합니다.
        """)
    public ApiResponse<PrepareUploadResponse> prepareUpload(
        @CurrentMember MemberPrincipal principal,
        @Valid @RequestBody PrepareUploadRequest request
    ) {
        FileUploadInfo uploadInfo = manageFileUseCase.getFileUploadUrl(
            request.toCommand(principal.getMemberId())
        );

        return ApiResponse.onSuccess(PrepareUploadResponse.from(uploadInfo));
    }

    /**
     * 파일 업로드 완료를 확인합니다.
     */
    @PostMapping("/{fileId}/confirm")
    @Operation(summary = "파일 업로드 완료 처리")
    public ApiResponse<Void> confirmUpload(@PathVariable String fileId) {
        manageFileUseCase.confirmUpload(fileId);
        return ApiResponse.onSuccess(null);
    }

    /**
     * 파일을 삭제합니다.
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제")
    public ApiResponse<Void> deleteFile(@PathVariable String fileId) {
        manageFileUseCase.deleteFile(fileId);
        return ApiResponse.onSuccess(null);
    }
}
