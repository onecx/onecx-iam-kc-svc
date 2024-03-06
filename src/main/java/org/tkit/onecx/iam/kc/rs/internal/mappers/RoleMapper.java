package org.tkit.onecx.iam.kc.rs.internal.mappers;

import org.keycloak.representations.idm.RoleRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.iam.kc.domain.model.PageResult;
import org.tkit.onecx.iam.kc.domain.model.RoleSearchCriteria;

import gen.org.tkit.onecx.iam.kc.internal.model.RolePageResultDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.RoleSearchCriteriaDTO;

@Mapper
public interface RoleMapper {
    RoleSearchCriteria map(RoleSearchCriteriaDTO roleSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTO map(PageResult<RoleRepresentation> result);
}
