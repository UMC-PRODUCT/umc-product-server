package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
<<<<<<< HEAD
import com.umc.product.organization.adapter.in.web.dto.response.GisuNameListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.GisuPageResponse;
=======
import com.umc.product.organization.adapter.in.web.dto.response.GisuListResponse;
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
<<<<<<< HEAD
import org.springframework.data.domain.Pageable;
=======
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48

@Tag(name = Constants.ORGANIZATION)
public interface AdminGisuQueryControllerApi {

<<<<<<< HEAD
    @Operation(summary = "기수 목록 조회 ", description = "기수 목록을 최신순(generation 내림차순)으로 페이징 조회합니다")
=======
    @Operation(summary = "기수 목록 조회 ", description = "전체 기수 목록을 최신순(generation 내림차순)으로 조회합니다")
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
<<<<<<< HEAD
                    content = @Content(schema = @Schema(implementation = GisuPageResponse.class))
            )
    })
    GisuPageResponse getGisuList(Pageable pageable);

    @Operation(summary = "기수 전체 목록 조회", description = "전체 기수 목록을 최신순(generation 내림차순)으로 조회합니다. 기수 ID와 기수 번호만 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GisuNameListResponse.class))
            )
    })
    GisuNameListResponse getAllGisu();
=======
                    content = @Content(schema = @Schema(implementation = GisuListResponse.class))
            )
    })
    GisuListResponse getGisuList();
>>>>>>> 5447cb8f1af6a362cee69dfbc502fd0ba238cd48
}
