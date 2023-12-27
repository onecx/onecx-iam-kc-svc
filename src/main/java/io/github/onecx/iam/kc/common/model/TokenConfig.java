package io.github.onecx.iam.kc.common.model;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.iam.kc")
public interface TokenConfig {

    @WithName("header.token")
    String headerToken();

    @WithName("token.verified")
    boolean tokenVerified();

    @WithName("token.issuer.public-key-location.suffix")
    String tokenPublicKeyLocationSuffix();

    @WithName("token.issuer.public-key-location.enabled")
    boolean tokenPublicKeyEnabled();
}
