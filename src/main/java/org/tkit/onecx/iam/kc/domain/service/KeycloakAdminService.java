package org.tkit.onecx.iam.kc.domain.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.tkit.onecx.iam.kc.domain.model.Page;
import org.tkit.onecx.iam.kc.domain.model.UserPageResult;
import org.tkit.onecx.iam.kc.domain.model.UserSearchCriteria;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogExclude;
import org.tkit.quarkus.log.cdi.LogService;

import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;

@LogService
@ApplicationScoped
public class KeycloakAdminService {

    @Inject
    Keycloak keycloak;

    public void resetPassword(@LogExclude(mask = "***") String value) {

        var context = ApplicationContext.get();
        var principalToken = context.getPrincipalToken();
        if (principalToken == null) {
            throw new KeycloakException("Principal token is required");
        }
        var realm = KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());

        CredentialRepresentation resetPassword = new CredentialRepresentation();
        resetPassword.setValue(value);
        resetPassword.setType(KeycloakAdminClientConfig.GrantType.PASSWORD.asString());
        resetPassword.setTemporary(false);
        keycloak.realm(realm).users().get(context.getPrincipal()).resetPassword(resetPassword);
    }

    public UserPageResult searchUsers(UserSearchCriteria criteria) {

        var context = ApplicationContext.get();
        var principalToken = context.getPrincipalToken();
        if (principalToken == null) {
            throw new KeycloakException("Principal token is required");
        }

        var realm = KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());

        var first = criteria.getPageNumber() * criteria.getPageSize();
        var count = keycloak.realm(realm).users().count(criteria.getQuery());

        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(criteria.getQuery(), first, criteria.getPageSize(), true);

        return new UserPageResult(count, users, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }

}
