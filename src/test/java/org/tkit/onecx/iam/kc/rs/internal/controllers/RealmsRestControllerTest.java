package org.tkit.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.iam.kc.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;

import gen.org.tkit.onecx.iam.kc.internal.model.RealmResponseDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(RealmRestController.class)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ia:read", "ocx-ia:write", "ocx-ia:all" })
class RealmsRestControllerTest extends AbstractTest {
    private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void findAllRealms_test() {

        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, keycloakClient.getAccessToken(USER_BOB))
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body().as(RealmResponseDTO.class);
        assertThat(result.getRealms()).hasSize(2);
    }

    @Test
    void findAllRealms_kc_exception_test() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, " ")
                .get()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
