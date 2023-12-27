package io.github.onecx.iam.kc.common.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;

import io.github.onecx.iam.kc.common.model.TokenConfig;
import io.github.onecx.iam.kc.common.model.TokenInfo;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class TokenService {

    @Inject
    JWTAuthContextInfo authContextInfo;

    @Inject
    TokenConfig config;

    @Inject
    JWTParser parser;

    @Context
    HttpHeaders headers;

    public TokenInfo getTokenInfo() {
        var apmPrincipalToken = headers.getHeaderString(config.headerToken());
        if (apmPrincipalToken == null || apmPrincipalToken.isBlank()) {
            log.error("Missing APM principal token: " + config.headerToken());
            throw new TokenException("Missing APM principal token");
        }
        try {
            return getTokenInfo(apmPrincipalToken);
        } catch (Exception ex) {
            throw new TokenException("Error parsing principal token", ex);
        }
    }

    private TokenInfo getTokenInfo(String apmPrincipalToken)
            throws InvalidJwtException, JoseException, MalformedClaimException, ParseException {

        String userId;
        String realmName;

        if (config.tokenVerified()) {
            var info = authContextInfo;

            // get public key location from issuer URL
            if (config.tokenPublicKeyEnabled()) {
                var jws = (JsonWebSignature) JsonWebStructure.fromCompactSerialization(apmPrincipalToken);
                var jwtClaims = JwtClaims.parse(jws.getUnverifiedPayload());
                var publicKeyLocation = jwtClaims.getIssuer() + config.tokenPublicKeyLocationSuffix();
                info = new JWTAuthContextInfo(authContextInfo);
                info.setPublicKeyLocation(publicKeyLocation);
            }

            var token = parser.parse(apmPrincipalToken, info);
            userId = token.getSubject();
            realmName = getRealmName(token.getIssuer());
        } else {

            var jws = (JsonWebSignature) JsonWebStructure.fromCompactSerialization(apmPrincipalToken);
            var jwtClaims = JwtClaims.parse(jws.getUnverifiedPayload());
            userId = jwtClaims.getSubject();
            realmName = getRealmName(jwtClaims.getIssuer());
        }

        return new TokenInfo(userId, realmName);
    }

    private static String getRealmName(String issuer) {

        int index = issuer.lastIndexOf("/");
        if (index < 0) {
            throw new TokenException("Wrong issuer format");
        }
        String realmName = issuer.substring(index + 1);
        if (realmName.isEmpty()) {
            throw new TokenException("Wrong issuer format");
        }

        return realmName;
    }

    public static class TokenException extends RuntimeException {

        public TokenException(String message) {
            super(message);
        }

        public TokenException(String message, Throwable t) {
            super(message, t);
        }
    }
}
