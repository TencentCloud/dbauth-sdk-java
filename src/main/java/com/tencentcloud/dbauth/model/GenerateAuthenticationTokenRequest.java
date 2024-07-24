package com.tencentcloud.dbauth.model;

import com.tencentcloudapi.cam.v20190116.CamErrorCode;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import org.apache.commons.lang3.StringUtils;

public final class GenerateAuthenticationTokenRequest {
    private final String region;
    private final String instanceId;
    private final String userName;
    private final Credential credential;
    private final ClientProfile clientProfile;

    private GenerateAuthenticationTokenRequest(Builder builder) throws TencentCloudSDKException {
        builder.checkInvalid();
        this.region = builder.region;
        this.instanceId = builder.instanceId;
        this.userName = builder.userName;
        this.credential = builder.credential;
        this.clientProfile = builder.clientProfile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String region() {
        return region;
    }

    public String instanceId() {
        return instanceId;
    }

    public String userName() {
        return userName;
    }

    public Credential credential() {
        return credential;
    }

    public ClientProfile clientProfile() {
        return clientProfile;
    }

    public static final class Builder {
        private String region;
        private String instanceId;
        private String userName;
        private Credential credential;
        private ClientProfile clientProfile;

        private Builder() {
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder credential(Credential credential) {
            this.credential = credential;
            return this;
        }

        /**
         * Set the client profile for the request. This is an optional configuration.
         * <p>
         * The client profile allows for additional customization of the request, such as setting
         * specific HTTP options. If not set, default settings are used.
         * </p>
         *
         * @param clientProfile The {@link ClientProfile} to be used with this request.
         * @return the Builder object
         */
        public Builder clientProfile(ClientProfile clientProfile) {
            this.clientProfile = clientProfile;
            return this;
        }

        /**
         * Check if the request is invalid.
         *
         * @throws TencentCloudSDKException if the request is invalid
         */
        public void checkInvalid() throws TencentCloudSDKException {
            if (StringUtils.isEmpty(region)) {
                throw new TencentCloudSDKException(
                        "The region is invalid.", "", CamErrorCode.INVALIDPARAMETER_RESOURCEREGIONERROR.getValue());
            }
            if (StringUtils.isEmpty(instanceId)) {
                throw new TencentCloudSDKException(
                        "The instanceId is invalid.", "", CamErrorCode.INVALIDPARAMETER_RESOURCEERROR.getValue());
            }
            if (StringUtils.isEmpty(userName)) {
                throw new TencentCloudSDKException(
                        "The userName is invalid.", "", CamErrorCode.INVALIDPARAMETER_USERNAMEILLEGAL.getValue());
            }
            if (credential == null || StringUtils.isEmpty(credential.getSecretId())
                    || StringUtils.isEmpty(credential.getSecretKey())) {
                throw new TencentCloudSDKException(
                        "The credential is invalid.", "", CamErrorCode.RESOURCENOTFOUND_SECRETNOTEXIST.getValue());
            }
        }

        public GenerateAuthenticationTokenRequest build() throws TencentCloudSDKException {
            return new GenerateAuthenticationTokenRequest(this);
        }
    }
}
