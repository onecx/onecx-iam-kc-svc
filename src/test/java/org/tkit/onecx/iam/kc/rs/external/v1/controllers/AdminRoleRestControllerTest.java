package org.tkit.onecx.iam.kc.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper.ErrorKeys.TOKEN_ERROR;
import static org.tkit.quarkus.rs.context.token.TokenParserService.ErrorKeys.ERROR_PARSE_TOKEN;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.test.AbstractTest;

import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.v1.model.RoleDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.RolePageResultDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.RoleSearchCriteriaDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(AdminRoleRestController.class)
class AdminRoleRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void roleSearchNoTokenTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .body(new RoleSearchCriteriaDTOV1())
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(TOKEN_ERROR.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("Principal token is required");
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();

        exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, " ")
                .body(new RoleSearchCriteriaDTOV1())
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(ERROR_PARSE_TOKEN.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("Error parse raw token");
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();

    }

    @Test
    void roleSearchNoBodyTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isNotNull()
                .isEqualTo("searchRolesByCriteria.roleSearchCriteriaDTOV1: must not be null");
        assertThat(exception.getInvalidParams()).isNotNull().isNotEmpty();
    }

    @Test
    void roleSearchEmptyResultTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTOV1().name("does-not-exists"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTOV1.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isEmpty();

    }

    @Test
    void roleSearchAllTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTOV1())
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTOV1.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty().hasSize(8);
    }

    @Test
    void roleSearchTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTOV1().name("onecx-admin"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTOV1.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty()
                .hasSize(1).contains(new RoleDTOV1().name("onecx-admin"));

        result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTOV1().name("onecx"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTOV1.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty()
                .hasSize(5).contains(
                        new RoleDTOV1().name("onecx-admin"),
                        new RoleDTOV1().name("onecx-portal-admin"),
                        new RoleDTOV1().name("onecx-portal-super-admin"),
                        new RoleDTOV1().name("onecx-test"),
                        new RoleDTOV1().name("onecx-user"));
    }
}
