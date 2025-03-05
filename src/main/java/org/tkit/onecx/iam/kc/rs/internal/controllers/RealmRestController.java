package org.tkit.onecx.iam.kc.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.kc.domain.service.KeycloakAdminService;
import org.tkit.onecx.iam.kc.rs.internal.mappers.ExceptionMapper;
import org.tkit.quarkus.rs.context.token.TokenException;

import gen.org.tkit.onecx.iam.kc.internal.RealmsInternalApi;
import gen.org.tkit.onecx.iam.kc.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RealmResponseDTO;

@ApplicationScoped
public class RealmRestController implements RealmsInternalApi {

    @Inject
    KeycloakAdminService adminService;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getAllRealms() {
        RealmResponseDTO realmResponseDTO = new RealmResponseDTO();
        realmResponseDTO.setRealms(adminService.getRealms());
        return Response.status(Response.Status.OK).entity(realmResponseDTO).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(TokenException ex) {
        return exceptionMapper.exception(ex.getKey(), ex.getMessage());
    }
}
