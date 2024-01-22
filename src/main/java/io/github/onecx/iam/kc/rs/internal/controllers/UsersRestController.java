package io.github.onecx.iam.kc.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.context.token.TokenException;

import gen.io.github.onecx.iam.kc.internal.UsersInternalApi;
import gen.io.github.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.domain.service.KeycloakAdminService;
import io.github.onecx.iam.kc.domain.service.KeycloakException;
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

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {
        var criteria = mapper.map(userSearchCriteriaDTO);
        var usersPage = adminService.searchUsers(criteria);
        return Response.ok(mapper.map(usersPage)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(TokenException ex) {
        return exceptionMapper.exception(ex.getKey(), ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(KeycloakException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
