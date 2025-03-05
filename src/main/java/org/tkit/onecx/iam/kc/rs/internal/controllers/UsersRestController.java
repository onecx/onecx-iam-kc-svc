package org.tkit.onecx.iam.kc.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.kc.domain.service.KeycloakAdminService;
import org.tkit.onecx.iam.kc.domain.service.KeycloakException;
import org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.rs.internal.mappers.UserMapper;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.context.token.TokenException;

import gen.org.tkit.onecx.iam.kc.internal.UsersInternalApi;
import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;

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
    public Response resetPassword(UserResetPasswordRequestDTO userResetPasswordRequestDTO) {
        adminService.resetPassword(userResetPasswordRequestDTO.getPassword());
        return Response.noContent().build();
    }

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {
        var criteria = mapper.map(userSearchCriteriaDTO);
        var usersPage = adminService.searchUsers(criteria);
        var realm = userSearchCriteriaDTO.getRealm() != null ? userSearchCriteriaDTO.getRealm()
                : adminService.getCurrentRealm();
        return Response.ok(mapper.map(usersPage, realm)).build();
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
