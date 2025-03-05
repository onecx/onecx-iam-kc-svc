package org.tkit.onecx.iam.kc.domain.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.*;
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

        var realm = getCurrentRealm();
        var principal = ApplicationContext.get().getPrincipal();

        CredentialRepresentation resetPassword = new CredentialRepresentation();
        resetPassword.setValue(value);
        resetPassword.setType(KeycloakAdminClientConfig.GrantType.PASSWORD.asString());
        resetPassword.setTemporary(false);
        keycloak.realm(realm).users().get(principal).resetPassword(resetPassword);
    }

    public PageResult<RoleRepresentation> searchRoles(RoleSearchCriteria criteria) {
        var realm = getCurrentRealm();

        var first = criteria.getPageNumber() * criteria.getPageSize();
        var count = 0;

        List<RoleRepresentation> roles = keycloak.realm(realm)
                .roles().list(criteria.getName(), first, criteria.getPageSize(), true);

        return new PageResult<>(count, roles, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }

    public PageResult<UserRepresentation> searchUsers(UserSearchCriteria criteria) {
        var realm = "";
        if (criteria.getRealm() == null || criteria.getRealm().isBlank()) {
            realm = getCurrentRealm();
        } else {
            realm = criteria.getRealm();
        }

        if (criteria.getUserId() != null && !criteria.getUserId().isBlank()) {
            return new PageResult<>(1, List.of(getUserById(criteria.getUserId(), realm)), Page.of(0, 1));
        }

        var first = criteria.getPageNumber() * criteria.getPageSize();
        var count = keycloak.realm(realm).users().count(criteria.getLastName(), criteria.getFirstName(), criteria.getEmail(),
                criteria.getUserName());

        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(criteria.getUserName(), criteria.getFirstName(), criteria.getLastName(), criteria.getEmail(), first,
                        criteria.getPageSize(), null, false);

        return new PageResult<>(count, users, Page.of(criteria.getPageNumber(), criteria.getPageSize()));
    }

    public UserRepresentation getUserById(String userId, String realm) {
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

    public String getCurrentRealm() {
        var principalToken = principalToken();
        return KeycloakRealmNameUtil.getRealmName(principalToken.getIssuer());
    }

    public List<RoleRepresentation> getUserRoles(String userId) {
        var realm = getCurrentRealm();

        MappingsRepresentation roles = keycloak.realm(realm)
                .users().get(userId).roles().getAll();

        return roles.getRealmMappings();
    }

    public List<String> getRealms() {
        return keycloak.realms().findAll().stream().map(RealmRepresentation::getRealm).toList();
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
