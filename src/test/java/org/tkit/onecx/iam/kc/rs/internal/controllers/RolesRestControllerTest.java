package org.tkit.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper.ErrorKeys.TOKEN_ERROR;
import static org.tkit.quarkus.rs.context.token.TokenParserService.ErrorKeys.ERROR_PARSE_TOKEN;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.core.Response;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RolePageResultDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.v1.model.UserRolesResponseDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(RolesRestController.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ia:read", "ocx-ia:write", "ocx-ia:all" })
class RolesRestControllerTest extends AbstractTest {

    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void roleSearchNoTokenTest() {

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(new RoleSearchCriteriaDTO())
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isNotNull().isEqualTo(TOKEN_ERROR.name());
        assertThat(exception.getDetail()).isNotNull().isEqualTo("Principal token is required");
        assertThat(exception.getInvalidParams()).isNotNull().isEmpty();

        exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, " ")
                .body(new RoleSearchCriteriaDTO())
                .post("/search")
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .post("/search")
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("does-not-exists"))
                .post("/search")
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO())
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty().hasSize(8);
    }

    @Test
    void roleSearchTest() {

        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("onecx-admin"))
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RolePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getStream()).isNotNull().isNotEmpty()
                .hasSize(1).contains(new RoleDTO().name("onecx-admin"));

        result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .body(new RoleSearchCriteriaDTO().name("onecx"))
                .post("/search")
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
                .pathParam("userId", id)
                .contentType(APPLICATION_JSON).get("/{userId}")
                .then().statusCode(Response.Status.OK.getStatusCode())
                .extract().as(UserRolesResponseDTOV1.class);
        System.out.println("ROLES: " + result.getRoles());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getRoles().size());
    }
}
