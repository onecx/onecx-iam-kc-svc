package io.github.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gen.io.github.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserPageResultDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.test.AbstractTest;
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
        Assertions.assertEquals("TOKEN_ERROR", exception.getErrorCode());
        Assertions.assertEquals(
                "Missing APM principal token",
                exception.getDetail());
        Assertions.assertNull(exception.getInvalidParams());

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
        Assertions.assertEquals("TOKEN_ERROR", exception.getErrorCode());
        Assertions.assertEquals(
                "Missing APM principal token",
                exception.getDetail());
        Assertions.assertNull(exception.getInvalidParams());

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
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", exception.getErrorCode());
        Assertions.assertEquals(
                "searchUsersByCriteria.userSearchCriteriaDTO: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }
}
