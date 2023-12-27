package io.github.onecx.iam.kc.domain.model;

import java.util.List;

import org.keycloak.representations.idm.UserRepresentation;

import lombok.Getter;

@Getter
public class UserPageResult {

    public static UserPageResult empty() {
        return new UserPageResult(0, List.of(), Page.of(0, 1));
    }

    private final long totalElements;

    private final long number;

    private final long size;

    private final long totalPages;

    private final List<UserRepresentation> stream;

    public UserPageResult(long totalElements, List<UserRepresentation> stream, Page page) {
        this.totalElements = totalElements;
        this.stream = stream;
        this.number = page.number();
        this.size = page.size();
        this.totalPages = (totalElements + size - 1) / size;
    }

    public boolean isEmpty() {
        return totalElements <= 0;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "c=" + totalElements +
                ",n=" + number +
                ",s=" + size +
                '}';
    }
}
