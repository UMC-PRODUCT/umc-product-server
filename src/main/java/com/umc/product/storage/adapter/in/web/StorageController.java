package com.umc.product.storage.adapter.in.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.storage.adapter.in.web.dto.PrepareUploadRequest;
import com.umc.product.storage.adapter.in.web.dto.PrepareUploadResponse;
import com.umc.product.storage.application.port.in.command.ManageFileUseCase;
import com.umc.product.storage.application.port.in.command.dto.DeleteFileCommand;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "Storage | 파일", description = "파일 업로드와 삭제를 다룹니다.")
public class StorageController {

    private final ManageFileUseCase manageFileUseCase;
    private final GetFileUseCase getFileUseCase;

    /**
     * 파일 업로드를 위한 Signed URL을 생성합니다.
     */
    @PostMapping("/prepare-upload")
    @Operation(operationId = "STORAGE-001", summary = "파일 업로드용 Signed URL 생성", description = """
        파일 카테고리는 Schema를 참고합니다.

        파일 업로드 후 "파일 업로드 완료 처리"를 호출해야 등록이 끝납니다.

        Content-Type에는 MIME 타입을, fileName에는 확장자를 포함한 파일명을 전달합니다.
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
    @Operation(operationId = "STORAGE-002", summary = "파일 업로드 완료 등록")
    public ApiResponse<Void> confirmUpload(@PathVariable String fileId) {
        manageFileUseCase.confirmUpload(fileId);
        return ApiResponse.onSuccess(null);
    }

    /**
     * 파일을 삭제합니다.
     */
    @DeleteMapping("/{fileId}")
    @Operation(operationId = "STORAGE-003", summary = "파일 삭제")
    public ApiResponse<Void> deleteFile(
        @CurrentMember MemberPrincipal principal,
        @PathVariable String fileId
    ) {
        manageFileUseCase.deleteFile(DeleteFileCommand.builder()
            .fileId(fileId)
            .requesterMemberId(principal.getMemberId())
            .build());
        return ApiResponse.onSuccess(null);
    }
}
