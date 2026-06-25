package com.umc.product.organization.adapter.in.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.umc.product.organization.adapter.in.graphql.dto.ChapterGraphQlResponse;
import com.umc.product.organization.adapter.in.graphql.dto.GisuGraphQlResponse;
import com.umc.product.organization.adapter.in.graphql.dto.GisuOrganizationGraphQlRequest;
import com.umc.product.organization.adapter.in.graphql.dto.GisuOrganizationPayloadGraphQlResponse;
import com.umc.product.organization.adapter.in.graphql.dto.SchoolDetailGraphQlResponse;
import com.umc.product.organization.adapter.in.graphql.dto.SchoolNameGraphQlResponse;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuOrganizationUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrganizationGraphQlController {

    private final GetGisuOrganizationUseCase getGisuOrganizationUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    @QueryMapping
    public GisuOrganizationPayloadGraphQlResponse gisuOrganizations(
        @Argument GisuOrganizationGraphQlRequest input
    ) {
        return GisuOrganizationPayloadGraphQlResponse.from(
            getGisuOrganizationUseCase.get(input.toQuery())
        );
    }

    @QueryMapping
    public GisuGraphQlResponse gisu(@Argument Long id) {
        return GisuGraphQlResponse.from(getGisuUseCase.getById(id));
    }

    @QueryMapping
    public GisuGraphQlResponse activeGisu() {
        return GisuGraphQlResponse.from(getGisuUseCase.getActiveGisu());
    }

    @QueryMapping
    public List<ChapterGraphQlResponse> chapters() {
        return getChapterUseCase.getAllChapters().stream()
            .map(ChapterGraphQlResponse::from)
            .toList();
    }

    @QueryMapping
    public ChapterGraphQlResponse chapter(@Argument Long id) {
        return ChapterGraphQlResponse.from(getChapterUseCase.getChapterById(id));
    }

    @QueryMapping
    public List<SchoolNameGraphQlResponse> schools() {
        return getSchoolUseCase.getAllSchoolNames().stream()
            .map(SchoolNameGraphQlResponse::from)
            .toList();
    }

    @QueryMapping
    public SchoolDetailGraphQlResponse school(@Argument Long id) {
        return SchoolDetailGraphQlResponse.from(getSchoolUseCase.getSchoolDetail(id));
    }
}
