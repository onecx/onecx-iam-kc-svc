package io.github.onecx.iam.kc.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.iam.kc.internal.UsersInternalApi;
import gen.io.github.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.common.model.TokenInfo;
import io.github.onecx.iam.kc.common.service.TokenService;
import io.github.onecx.iam.kc.domain.service.KeycloakAdminService;
import io.github.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import io.github.onecx.iam.kc.rs.internal.mappers.UserMapper;

@LogService
@ApplicationScoped
public class UsersRestController implements UsersInternalApi {

    @Inject
    KeycloakAdminService adminService;

    @Inject
    UserMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    TokenService tokenService;

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {
        TokenInfo tokenInfo = tokenService.getTokenInfo();
        var criteria = mapper.map(userSearchCriteriaDTO, tokenInfo.realmName());
        var usersPage = adminService.searchUsers(criteria);
        return Response.ok(mapper.map(usersPage)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(TokenService.TokenException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
