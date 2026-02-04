package com.umc.product.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.organization.adapter.in.web.AdminChapterController;
import com.umc.product.organization.adapter.in.web.AdminGisuController;
import com.umc.product.organization.adapter.in.web.AdminGisuQueryController;
import com.umc.product.organization.adapter.in.web.AdminSchoolController;
import com.umc.product.organization.adapter.in.web.AdminSchoolQueryController;
import com.umc.product.organization.adapter.in.web.ChapterQueryController;
import com.umc.product.organization.adapter.in.web.SchoolQueryController;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
    AdminSchoolController.class,
    SchoolQueryController.class,
    AdminSchoolQueryController.class,
    AdminChapterController.class,
    ChapterQueryController.class,
    AdminGisuController.class,
    AdminGisuQueryController.class,
})
@Import({
    RestDocsConfig.class,
    JacksonConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class DocumentationTest {
    // =================================================
    // Rest Docs 생성을 위한 기본적인 주입을 처리하는 부모 클래스
    // =================================================

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected ManageSchoolUseCase manageSchoolUseCase;

    @MockitoBean
    protected GetSchoolUseCase getSchoolUseCase;

    @MockitoBean
    protected ManageChapterUseCase manageChapterUseCase;

    @MockitoBean
    protected GetChapterUseCase getChapterUseCase;

    @MockitoBean
    protected ManageGisuUseCase manageGisuUseCase;

    @MockitoBean
    protected GetGisuUseCase getGisuUseCase;

    @MockitoBean
    protected GetFileUseCase getFileUseCase;
}
