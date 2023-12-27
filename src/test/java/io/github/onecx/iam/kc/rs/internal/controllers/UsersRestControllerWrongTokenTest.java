package io.github.onecx.iam.kc.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.security.PrivateKey;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.jwt.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gen.io.github.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.common.model.TokenConfig;
import io.github.onecx.iam.kc.test.AbstractTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@QuarkusTest
@TestHTTPEndpoint(UsersRestController.class)
class UsersRestControllerWrongTokenTest extends AbstractTest {
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
    void testIssuerParserError() {
        UserSearchCriteriaDTO dto = new UserSearchCriteriaDTO();
        dto.setQuery("bob");

        var exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, createToken("bob", "does-not-exists"))
                .body(dto)
                .post()
                .then().log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("TOKEN_ERROR", exception.getErrorCode());
        Assertions.assertEquals(
                "Error parsing principal token",
                exception.getDetail());

        exception = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_TOKEN, createToken("bob", "does-not-exists/"))
                .body(dto)
                .post()
                .then().log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract()
                .body().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("TOKEN_ERROR", exception.getErrorCode());
        Assertions.assertEquals(
                "Error parsing principal token",
                exception.getDetail());
    }

    protected static String createToken(String sub, String issuer) {
        try {
            JsonObjectBuilder claims = Json.createObjectBuilder();
            claims.add(Claims.preferred_username.name(), sub);
            claims.add(Claims.sub.name(), sub);
            claims.add(Claims.iss.name(), issuer);
            PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
            return Jwt.claims(claims.build()).sign(privateKey);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
