package com.aws.demo;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.services.securitytoken.model.Credentials;
import lombok.Data;

@Data
public class AssumedRoleCredentials implements AWSSessionCredentials {
    private String aWSAccessKeyId;
    private String aWSSecretKey;
    private String sessionToken;

    public AssumedRoleCredentials(Credentials credentials) {
        this.aWSAccessKeyId = credentials.getAccessKeyId();
        this.aWSSecretKey = credentials.getSecretAccessKey();
        this.sessionToken = credentials.getSessionToken();
    }
}
