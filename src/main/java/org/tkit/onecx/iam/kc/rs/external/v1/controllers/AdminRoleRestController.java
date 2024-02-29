package org.tkit.onecx.iam.kc.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.kc.domain.service.KeycloakAdminService;
import org.tkit.onecx.iam.kc.domain.service.KeycloakException;
import org.tkit.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper;
import org.tkit.onecx.iam.kc.rs.external.v1.mappers.KeycloakMapper;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.context.token.TokenException;

import gen.org.tkit.onecx.iam.kc.v1.AdminRoleControllerApi;
import gen.org.tkit.onecx.iam.kc.v1.model.ProblemDetailResponseDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.RoleSearchCriteriaDTOV1;

@LogService
@ApplicationScoped
public class AdminRoleRestController implements AdminRoleControllerApi {

    @Inject
    KeycloakAdminService adminService;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    KeycloakMapper mapper;

    @Override
    public Response searchRolesByCriteria(RoleSearchCriteriaDTOV1 roleSearchCriteriaDTOV1) {
        var criteria = mapper.map(roleSearchCriteriaDTOV1);
        var result = adminService.searchRoles(criteria);
        return Response.ok(mapper.map(result)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(TokenException ex) {
        return exceptionMapper.exception(ex.getKey(), ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(KeycloakException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
