package io.github.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gen.io.github.onecx.iam.kc.internal.model.UserPageResultDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.common.model.TokenConfig;
import io.github.onecx.iam.kc.test.AbstractTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(UsersRestController.class)
class UsersRestControllerConfigTest extends AbstractTest {

    @InjectMock
    TokenConfig tokenConfig;

    @Inject
    Config config;

    @BeforeEach
    void beforeEach() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(TokenConfig.class);
        Mockito.when(tokenConfig.headerToken()).thenReturn(tmp.headerToken());
        Mockito.when(tokenConfig.tokenVerified()).thenReturn(false);
        Mockito.when(tokenConfig.tokenPublicKeyLocationSuffix()).thenReturn(tmp.tokenPublicKeyLocationSuffix());
        Mockito.when(tokenConfig.tokenPublicKeyEnabled()).thenReturn(tmp.tokenPublicKeyEnabled());
    }

    @Test
    void skipTokenVerified() {

        KeycloakTestClient keycloakClient = new KeycloakTestClient();
        var token = keycloakClient.getAccessToken("bob");

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
}
