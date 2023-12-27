package io.github.onecx.iam.kc.domain.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.tkit.quarkus.log.cdi.LogExclude;
import org.tkit.quarkus.log.cdi.LogService;

import io.github.onecx.iam.kc.common.model.TokenInfo;
import io.github.onecx.iam.kc.domain.model.Page;
import io.github.onecx.iam.kc.domain.model.UserPageResult;
import io.github.onecx.iam.kc.domain.model.UserSearchCriteria;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;

@LogService
@ApplicationScoped
public class KeycloakAdminService {

    @Inject
    Keycloak keycloak;

    public void resetPassword(TokenInfo tokenInfo, @LogExclude(mask = "***") String value) {
        CredentialRepresentation resetPassword = new CredentialRepresentation();
        resetPassword.setValue(value);
        resetPassword.setType(KeycloakAdminClientConfig.GrantType.PASSWORD.asString());
        resetPassword.setTemporary(false);
        keycloak.realm(tokenInfo.realmName()).users().get(tokenInfo.userId()).resetPassword(resetPassword);
    }

    public UserPageResult searchUsers(UserSearchCriteria criteria) {

        var first = criteria.getPageNumber() * criteria.getPageSize();
        var count = keycloak.realm(criteria.getRealmName()).users().count(criteria.getQuery());

        List<UserRepresentation> users = keycloak.realm(criteria.getRealmName())
                .users()
                .search(criteria.getQuery(), first, criteria.getPageSize(), true);

        return new UserPageResult(count, users, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }
}
