package org.tkit.onecx.iam.kc.rs.external.v1.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.iam.kc.v1.model.RoleSearchCriteriaDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.UserResetPasswordRequestDTOV1;

@ApplicationScoped
public class ExternalLogParam implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(

                item(10, RoleSearchCriteriaDTOV1.class, x -> {
                    RoleSearchCriteriaDTOV1 d = (RoleSearchCriteriaDTOV1) x;
                    return RoleSearchCriteriaDTOV1.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize()
                            + "]";
                }),

                item(10, UserResetPasswordRequestDTOV1.class, x -> UserResetPasswordRequestDTOV1.class.getSimpleName()));
    }
}
