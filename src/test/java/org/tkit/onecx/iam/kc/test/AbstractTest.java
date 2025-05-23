package org.tkit.onecx.iam.kc.test;

import static org.keycloak.common.util.Encode.urlEncode;

import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractTest {

    protected static final String APM_HEADER_TOKEN = "apm-principal-token";

    DevServicesContext testContext;
    public static final String CLIENT_ID_PROP = "quarkus.oidc.client-id";
    public static final String USER_BOB = "bob";
    public static final String USER_ALICE = "alice";

    protected String getClientId() {
        return getPropertyValue(CLIENT_ID_PROP, "quarkus-app");
    }

    protected String getPropertyValue(String prop, String defaultValue) {
        return ConfigProvider.getConfig().getOptionalValue(prop, String.class)
                .orElseGet(() -> getDevProperty(prop, defaultValue));
    }

    private String getDevProperty(String prop, String defaultValue) {
        String value = testContext == null ? null : testContext.devServicesProperties().get(prop);
        return value == null ? defaultValue : value;
    }

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    protected String getKeycloakClientToken(String clientId) {
        return keycloakClient.getClientAccessToken(clientId);
    }

    protected AccessTokenResponse getTokens(KeycloakTestClient ktc, String userName) {
        return getTokens(ktc, userName, userName);
    }

    protected AccessTokenResponse getTokens(KeycloakTestClient ktc, String userName, String password) {

        String clientId = "quarkus-app";
        String clientSecret = "secret";
        List<String> scopes = List.of("openid");

        String authServerUrl = ktc.getAuthServerUrl();

        RequestSpecification requestSpec = RestAssured.given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", password)
                .param("client_id", clientId)
                .param("client_secret", clientSecret)
                .param("scope", urlEncode(String.join(" ", scopes)));

        return requestSpec.when().post(authServerUrl + "/protocol/openid-connect/token")
                .as(AccessTokenResponse.class);
    }
}
