package io.github.onecx.iam.kc.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.github.onecx.iam.kc.common.model.TokenConfig;
import io.quarkus.test.Mock;
import io.quarkus.test.common.DevServicesContext;
import io.smallrye.config.SmallRyeConfig;

public abstract class AbstractTest {

    protected static final String APM_HEADER_TOKEN = ConfigProvider.getConfig().getValue("onecx.iam.kc.header.token",
            String.class);

    DevServicesContext testContext;
    public static String CLIENT_ID_PROP = "quarkus.oidc.client-id";
    public static String USER_BOB = "bob";
    public static String USER_ALICE = "alice";

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

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        TokenConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(TokenConfig.class);
        }
    }

}
