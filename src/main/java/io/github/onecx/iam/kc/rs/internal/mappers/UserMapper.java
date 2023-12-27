package io.github.onecx.iam.kc.rs.internal.mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.io.github.onecx.iam.kc.internal.model.UserDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserPageResultDTO;
import gen.io.github.onecx.iam.kc.internal.model.UserSearchCriteriaDTO;
import io.github.onecx.iam.kc.domain.model.UserPageResult;
import io.github.onecx.iam.kc.domain.model.UserSearchCriteria;

@Mapper
public interface UserMapper {

    default UserSearchCriteria map(UserSearchCriteriaDTO dto, String realName) {
        var result = mapDto(dto);
        result.setRealmName(realName);
        return result;
    }

    @Mapping(target = "realmName", ignore = true)
    UserSearchCriteria mapDto(UserSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserPageResultDTO map(UserPageResult pageResult);

    UserDTO map(UserRepresentation user);

    default OffsetDateTime map(Long dateTime) {
        if (dateTime == null) {
            return null;
        }
        var tmp = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime),
                TimeZone.getDefault().toZoneId());

        return OffsetDateTime.of(tmp, ZoneOffset.systemDefault().getRules().getOffset(tmp));
    }
}
