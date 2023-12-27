package io.github.onecx.iam.kc.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCriteria {

    private String realmName;

    private String query;

    private Integer pageNumber = 0;

    private Integer pageSize = 10;
}
