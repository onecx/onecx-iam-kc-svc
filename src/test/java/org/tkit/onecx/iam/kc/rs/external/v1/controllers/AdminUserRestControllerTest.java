package org.tkit.onecx.iam.kc.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS;
import static org.tkit.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper.ErrorKeys.TOKEN_ERROR;

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.core.Response;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.tkit.onecx.iam.kc.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.v1.model.UserResetPasswordRequestDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.UserRolesResponseDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(AdminUserRestController.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ia:read", "ocx-ia:write" })
class AdminUserRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static String token;

    @BeforeAll
    static void setUp() {
        token = keycloakClient.getAccessToken(USER_ALICE);
    }

    @Test
    void resetPasswordTest() {
        var tokens = this.getTokens(keycloakClient, USER_BOB);
        var bobToken = tokens.getIdToken();

        UserResetPasswordRequestDTOV1 dto = new UserResetPasswordRequestDTOV1();
        dto.setPassword("changedPassword");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, bobToken)
                .body(dto)
                .put("/password")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        var tmp = keycloakClient.getAccessToken(USER_BOB);
        Assertions.assertNull(tmp);

        bobToken = getTokens(keycloakClient, USER_BOB, dto.getPassword()).getIdToken();
        dto.setPassword(USER_BOB);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, bobToken)
                .body(dto)
                .put("/password")
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(dto)
                .put("/password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(TOKEN_ERROR.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "Principal token is required",
                exception.getDetail());
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();
    }

    @Test
    void resetPasswordEmptyRequestTest() {

        UserResetPasswordRequestDTOV1 dto = new UserResetPasswordRequestDTOV1();

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, token)
                .body(dto)
                .put("/password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(CONSTRAINT_VIOLATIONS.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "userResetPassword.userResetPasswordRequestDTOV1.password: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }

    @Test
    void resetPasswordNoRequestTest() {

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .put("/password")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(CONSTRAINT_VIOLATIONS.name(), exception.getErrorCode());
        Assertions.assertEquals(
                "userResetPassword.userResetPasswordRequestDTOV1: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
    }

    @Test
    void getUserRolesTest() throws IOException {
        var tokens = this.getTokens(keycloakClient, USER_ALICE);
        var aliceToken = tokens.getIdToken();
        ObjectMapper mapper = new ObjectMapper();
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = aliceToken.split("\\.");
        String body = new String(decoder.decode(chunks[1]));
        JSONObject jwt = mapper.readValue(body, JSONObject.class);

        String id = jwt.get("sub").toString();

        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_TOKEN, aliceToken)
                .contentType(APPLICATION_JSON).get("/roles/" + id)
                .then().statusCode(Response.Status.OK.getStatusCode())
                .extract().as(UserRolesResponseDTOV1.class);
        System.out.println("ROLES: " + result.getRoles());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getRoles().size());
    }
}
