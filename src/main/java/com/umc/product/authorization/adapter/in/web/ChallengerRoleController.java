package com.umc.product.authorization.adapter.in.web;

import com.umc.product.authorization.adapter.in.web.dto.request.CreateChallengerRoleRequest;
import com.umc.product.authorization.adapter.in.web.dto.response.ChallengerRoleResponse;
import com.umc.product.authorization.adapter.in.web.dto.response.CreateChallengerRoleResponse;
import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.DeleteChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "챌린저 역할 생성", description = "운영진 권한 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization/challenger-role")
public class ChallengerRoleController {

    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    private final GetGisuUseCase getGisuUseCase;

    @PostMapping
    CreateChallengerRoleResponse createChallengerRole(CreateChallengerRoleRequest request) {
        Long createdId =
            manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.from(request));

        return CreateChallengerRoleResponse.builder()
            .challengerRoleId(createdId)
            .build();
    }

    @GetMapping("{challengerRoleId}")
    ChallengerRoleResponse getChallengerRole(
        @PathVariable Long challengerRoleId
    ) {
        ChallengerRoleInfo challengerRoleInfo = getChallengerRoleUseCase.byId(challengerRoleId);

        return ChallengerRoleResponse.from(
            challengerRoleInfo, getGisuInfo(challengerRoleInfo.gisuId())
        );
    }

    @DeleteMapping("{challengerRoleId}")
    void deleteChallengerRole(
        @PathVariable Long challengerRoleId
    ) {
        manageChallengerRoleUseCase.deleteChallengerRole(
            DeleteChallengerRoleCommand.builder()
                .challengerRoleId(challengerRoleId)
                .build()
        );
    }


    private GisuInfo getGisuInfo(Long gisuId) {
        return getGisuUseCase.getById(gisuId);
    }
}
