package org.tkit.onecx.iam.kc.rs.external.v1.mappers;

import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.iam.kc.domain.model.PageResult;
import org.tkit.onecx.iam.kc.domain.model.RoleSearchCriteria;

import gen.org.tkit.onecx.iam.kc.v1.model.RoleDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.RolePageResultDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.RoleSearchCriteriaDTOV1;
import gen.org.tkit.onecx.iam.kc.v1.model.UserRolesResponseDTOV1;

@Mapper
public interface KeycloakMapper {

    RoleSearchCriteria map(RoleSearchCriteriaDTOV1 dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTOV1 map(PageResult<RoleRepresentation> data);

    RoleDTOV1 map(RoleRepresentation user);

    default UserRolesResponseDTOV1 map(List<RoleRepresentation> userRoles) {
        UserRolesResponseDTOV1 responseDTO = new UserRolesResponseDTOV1();
        responseDTO.setRoles(mapRoles(userRoles));
        return responseDTO;
    }

    List<RoleDTOV1> mapRoles(List<RoleRepresentation> roles);
}
