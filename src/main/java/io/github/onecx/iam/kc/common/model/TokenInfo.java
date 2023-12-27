package io.github.onecx.iam.kc.common.model;

public record TokenInfo(String userId, String realmName) {

    @Override
    public String toString() {
        return "TokenInfo{userId='" + userId + "',realm='" + realmName + "'}";
    }
}
