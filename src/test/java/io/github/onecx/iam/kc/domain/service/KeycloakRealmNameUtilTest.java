package io.github.onecx.iam.kc.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.onecx.iam.kc.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class KeycloakRealmNameUtilTest extends AbstractTest {

    @Test
    void realmNameTest() {

        Assertions.assertThrowsExactly(KeycloakException.class, () -> KeycloakRealmNameUtil.getRealmName(""));
    }
}
