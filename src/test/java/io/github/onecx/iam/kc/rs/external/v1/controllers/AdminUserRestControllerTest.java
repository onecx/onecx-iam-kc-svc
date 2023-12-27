package io.github.onecx.iam.kc.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gen.io.github.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.iam.kc.v1.model.UserResetPasswordRequestDTOV1;
import io.github.onecx.iam.kc.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(AdminUserRestController.class)
class AdminUserRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static String token;

    @BeforeAll
    static void setUp() {
        token = keycloakClient.getAccessToken(USER_ALICE);
    }

    @Test
    void resetPasswordTest() {
        var bobToken = keycloakClient.getAccessToken(USER_BOB);

        UserResetPasswordRequestDTOV1 dto = new UserResetPasswordRequestDTOV1();
        dto.setPassword("changedPassword");

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, bobToken)
                .body(dto)
                .put()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        var tmp = keycloakClient.getAccessToken(USER_BOB);
        Assertions.assertNull(tmp);

        bobToken = keycloakClient.getAccessToken(USER_BOB, dto.getPassword(), getClientId());
        dto.setPassword(USER_BOB);

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, bobToken)
                .body(dto)
                .put()
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        tmp = keycloakClient.getAccessToken(USER_BOB);
        Assertions.assertNotNull(tmp);
    }

    @Test
    void resetPasswordNoTokenTest() {

        UserResetPasswordRequestDTOV1 dto = new UserResetPasswordRequestDTOV1();
        dto.setPassword("*******");

        var exception = given()
                .contentType(APPLICATION_JSON)
                .body(dto)
                .put()
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
    void resetPasswordEmptyRequestTest() {

        UserResetPasswordRequestDTOV1 dto = new UserResetPasswordRequestDTOV1();

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, token)
                .body(dto)
                .put()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", exception.getErrorCode());
        Assertions.assertEquals(
                "resetPassword.userResetPasswordRequestDTOV1.password: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }

    @Test
    void resetPasswordNoRequestTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .put()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", exception.getErrorCode());
        Assertions.assertEquals(
                "resetPassword.userResetPasswordRequestDTOV1: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }
}
