package org.tkit.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS;
import static org.tkit.quarkus.rs.context.token.TokenParserService.ErrorKeys.ERROR_PARSE_TOKEN;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.test.AbstractTest;

import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserPageResultDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(UsersRestController.class)
class UsersRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static String token;

    @BeforeAll
    static void setUp() {
        token = keycloakClient.getAccessToken(USER_ALICE);
    }

    @Test
    void searchUsersRequest() {

        UserSearchCriteriaDTO dto = new UserSearchCriteriaDTO();
        dto.setQuery("bob");

        var result = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, token)
                .body(dto)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(UserPageResultDTO.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertNotNull(result.getStream());
        Assertions.assertEquals(1, result.getStream().size());
        Assertions.assertEquals("bob", result.getStream().get(0).getUsername());
    }

    @Test
    void searchUsersEmptyToken() {

        UserSearchCriteriaDTO dto = new UserSearchCriteriaDTO();

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, " ")
                .body(dto)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ERROR_PARSE_TOKEN.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "Error parse raw token",
                exception.getDetail());
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();

    }

    @Test
    void searchUsersNoToken() {

        UserSearchCriteriaDTO dto = new UserSearchCriteriaDTO();

        var exception = given()
                .contentType(APPLICATION_JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ExceptionMapper.ErrorKeys.TOKEN_ERROR.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "Principal token is required",
                exception.getDetail());
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();

    }

    @Test
    void searchUsersNoRequest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(CONSTRAINT_VIOLATIONS.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "searchUsersByCriteria.userSearchCriteriaDTO: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }
}
