package org.tkit.onecx.iam.kc.domain.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.tkit.onecx.iam.kc.domain.model.Page;
import org.tkit.onecx.iam.kc.domain.model.PageResult;
import org.tkit.onecx.iam.kc.domain.model.RoleSearchCriteria;
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

        var principalToken = principalToken();
        var realm = KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());
        var principal = ApplicationContext.get().getPrincipal();

        CredentialRepresentation resetPassword = new CredentialRepresentation();
        resetPassword.setValue(value);
        resetPassword.setType(KeycloakAdminClientConfig.GrantType.PASSWORD.asString());
        resetPassword.setTemporary(false);
        keycloak.realm(realm).users().get(principal).resetPassword(resetPassword);
    }

    public PageResult<RoleRepresentation> searchRoles(RoleSearchCriteria criteria) {

        var principalToken = principalToken();

        var realm = KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());

        var first = criteria.getPageNumber() * criteria.getPageSize();

        List<RoleRepresentation> roles = keycloak.realm(realm)
                .roles().list(criteria.getName(), first, criteria.getPageSize(), true);
        var count = roles.size();
        return new PageResult<>(count, roles, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }

    public PageResult<UserRepresentation> searchUsers(UserSearchCriteria criteria) {

        var principalToken = principalToken();

        var realm = KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());

        var first = criteria.getPageNumber() * criteria.getPageSize();
        var count = keycloak.realm(realm).users().count(criteria.getQuery());

        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(criteria.getQuery(), first, criteria.getPageSize(), true);

        return new PageResult<>(count, users, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }

    private JsonWebToken principalToken() {
        var context = ApplicationContext.get();
        var principalToken = context.getPrincipalToken();
        if (principalToken == null) {
            throw new KeycloakException("Principal token is required");
        }
        return principalToken;
    }
}
