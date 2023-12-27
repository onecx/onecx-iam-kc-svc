package io.github.onecx.iam.kc.rs.external.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.iam.kc.v1.model.UserResetPasswordRequestDTOV1;

@ApplicationScoped
public class ExternalLogParam implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, UserResetPasswordRequestDTOV1.class, x -> UserResetPasswordRequestDTOV1.class.getSimpleName()));
    }
}
