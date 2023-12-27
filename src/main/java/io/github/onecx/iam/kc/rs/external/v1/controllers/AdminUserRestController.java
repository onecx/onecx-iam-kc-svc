package io.github.onecx.iam.kc.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.iam.kc.v1.AdminUserControllerApi;
import gen.io.github.onecx.iam.kc.v1.model.ProblemDetailResponseDTOV1;
import gen.io.github.onecx.iam.kc.v1.model.UserResetPasswordRequestDTOV1;
import io.github.onecx.iam.kc.common.model.TokenInfo;
import io.github.onecx.iam.kc.common.service.TokenService;
import io.github.onecx.iam.kc.domain.service.KeycloakAdminService;
import io.github.onecx.iam.kc.rs.external.v1.mappers.ExceptionMapper;

@LogService
@ApplicationScoped
public class AdminUserRestController implements AdminUserControllerApi {

    @Inject
    KeycloakAdminService adminService;

    @Inject
    TokenService tokenService;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response resetPassword(UserResetPasswordRequestDTOV1 userResetPasswordRequestDTOV1) {
        TokenInfo tokenInfo = tokenService.getTokenInfo();
        adminService.resetPassword(tokenInfo, userResetPasswordRequestDTOV1.getPassword());
        return null;
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(TokenService.TokenException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTOV1> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
