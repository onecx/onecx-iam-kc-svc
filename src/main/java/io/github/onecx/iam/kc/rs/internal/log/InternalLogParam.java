package io.github.onecx.iam.kc.rs.internal.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;

@ApplicationScoped
public class InternalLogParam implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                item(10, UserSearchCriteriaDTO.class, x -> {
                    UserSearchCriteriaDTO d = (UserSearchCriteriaDTO) x;
                    return UserSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + "," + d.getPageSize() + "]";
                }));
    }
}
