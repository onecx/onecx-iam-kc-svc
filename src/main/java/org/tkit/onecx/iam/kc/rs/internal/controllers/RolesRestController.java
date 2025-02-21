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
import org.tkit.onecx.iam.kc.rs.internal.mappers.RoleMapper;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.context.token.TokenException;

import gen.org.tkit.onecx.iam.kc.internal.RolesInternalApi;
import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleSearchCriteriaDTO;

@LogService
@ApplicationScoped
public class RolesRestController implements RolesInternalApi {

    @Inject
    KeycloakAdminService adminService;

    @Inject
    RoleMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getUserRoles(String userId) {
        return Response.ok().entity(mapper.map(adminService.getUserRoles(userId))).build();
    }

    @Override
    public Response searchRolesByCriteria(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        var criteria = mapper.map(roleSearchCriteriaDTO);
        var result = adminService.searchRoles(criteria);
        return Response.ok(mapper.map(result)).build();
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
