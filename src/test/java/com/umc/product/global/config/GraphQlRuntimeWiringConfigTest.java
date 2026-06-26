package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.graphql.execution.GraphQlSource;

class GraphQlRuntimeWiringConfigTest {

    @Test
    @DisplayName("Project GraphQL schema는 Long scalar wiring과 함께 로드된다")
    void projectGraphQlSchemaLoadsWithLongScalar() throws IOException {
        Resource[] schemaResources = new PathMatchingResourcePatternResolver()
            .getResources("classpath*:graphql/**/*.graphqls");

        assertThat(Arrays.stream(schemaResources).map(Resource::getFilename))
            .contains("organization.graphqls", "project.graphqls");

        GraphQlSource graphQlSource = GraphQlSource.schemaResourceBuilder()
            .schemaResources(schemaResources)
            .configureRuntimeWiring(new GraphQlRuntimeWiringConfig().graphQlRuntimeWiringConfigurer())
            .build();

        assertThat(graphQlSource.schema().getType("Project")).isNotNull();
        assertThat(graphQlSource.schema().getType("Long")).isNotNull();
    }
}
