package org.tkit.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper.ErrorKeys.TOKEN_ERROR;
import static org.tkit.quarkus.rs.context.token.TokenParserService.ErrorKeys.ERROR_PARSE_TOKEN;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.test.AbstractTest;

import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RolePageResultDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleSearchCriteriaDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(RolesRestController.class)
class RolesRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void roleSearchNoTokenTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .body(new RoleSearchCriteriaDTO())
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(TOKEN_ERROR.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("Principal token is required");
        assertThat(exception.getInvalidParams()).isNull();

        exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, " ")
                .body(new RoleSearchCriteriaDTO())
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(ERROR_PARSE_TOKEN.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("Error parse raw token");
        assertThat(exception.getInvalidParams()).isNull();

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
                .isEqualTo("searchRolesByCriteria.roleSearchCriteriaDTO: must not be null");
        assertThat(exception.getInvalidParams()).isNotNull().isNotEmpty();
    }

    @Test
    void roleSearchEmptyResultTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("does-not-exists"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isEmpty();

    }

    @Test
    void roleSearchAllTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO())
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty().hasSize(8);
        assertThat(result.getTotalElements()).isEqualTo(8);
    }

    @Test
    void roleSearchTest() {

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("onecx-admin"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty()
                .hasSize(1).contains(new RoleDTO().name("onecx-admin"));

        result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("onecx"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty()
                .hasSize(5).contains(
                        new RoleDTO().name("onecx-admin"),
                        new RoleDTO().name("onecx-portal-admin"),
                        new RoleDTO().name("onecx-portal-super-admin"),
                        new RoleDTO().name("onecx-test"),
                        new RoleDTO().name("onecx-user"));
    }
}
