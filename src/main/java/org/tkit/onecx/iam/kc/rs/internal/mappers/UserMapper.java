package org.tkit.onecx.iam.kc.rs.internal.mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.iam.kc.domain.model.PageResult;
import org.tkit.onecx.iam.kc.domain.model.UserSearchCriteria;

import gen.org.tkit.onecx.iam.kc.internal.model.UserDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserPageResultDTO;
import gen.org.tkit.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;

@Mapper
public interface UserMapper {

    UserSearchCriteria map(UserSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserPageResultDTO map(PageResult<UserRepresentation> pageResult);

    @Mapping(target = "removeAttributesItem", ignore = true)
    UserDTO map(UserRepresentation user);

    default OffsetDateTime map(Long dateTime) {
        if (dateTime == null) {
            return null;
        }
        var tmp = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime),
                TimeZone.getDefault().toZoneId());

        return OffsetDateTime.of(tmp, ZoneId.systemDefault().getRules().getOffset(tmp));
    }
}
