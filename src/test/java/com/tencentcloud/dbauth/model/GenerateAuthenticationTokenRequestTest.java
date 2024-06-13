package com.tencentcloud.dbauth.model;

import com.tencentcloudapi.cam.v20190116.CamErrorCode;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.junit.Assert;
import org.junit.Test;

public class GenerateAuthenticationTokenRequestTest {

    @Test
    public void testBuilder() throws TencentCloudSDKException {
        String region = "ap-guangzhou";
        String instanceId = "instanceId";
        String userName = "test";
        Credential credential = new Credential("secretId", "secretKey");
        GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                .region(region)
                .credential(credential)
                .userName(userName)
                .instanceId(instanceId)
                .build();

        Assert.assertEquals(region, tokenRequest.region());
        Assert.assertEquals(instanceId, tokenRequest.instanceId());
        Assert.assertEquals(userName, tokenRequest.userName());
        Assert.assertEquals(credential, tokenRequest.credential());
    }

    @Test
    public void testBuilderWithInvalidRegion() {
        String region = "";
        String instanceId = "instanceId";
        String userName = "test";
        Credential credential = new Credential("secretId", "secretKey");
        try {
            GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .build();
        } catch (TencentCloudSDKException e) {
            Assert.assertEquals(CamErrorCode.INVALIDPARAMETER_RESOURCEREGIONERROR.getValue(), e.getErrorCode());
        }
    }

    @Test
    public void testBuilderWithInvalidInstanceId() {
        String region = "ap-guangzhou";
        String instanceId = "";
        String userName = "test";
        Credential credential = new Credential("secretId", "secretKey");
        try {
            GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .build();
        } catch (TencentCloudSDKException e) {
            Assert.assertEquals(CamErrorCode.INVALIDPARAMETER_RESOURCEERROR.getValue(), e.getErrorCode());
        }
    }

    @Test
    public void testBuilderWithInvalidUserName() {
        String region = "ap-guangzhou";
        String instanceId = "instanceId";
        String userName = "";
        Credential credential = new Credential("secretId", "secretKey");
        try {
            GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .build();
        } catch (TencentCloudSDKException e) {
            Assert.assertEquals(CamErrorCode.INVALIDPARAMETER_USERNAMEILLEGAL.getValue(), e.getErrorCode());
        }
    }

    @Test
    public void testBuilderWithInvalidCredential() {
        String region = "ap-guangzhou";
        String instanceId = "instanceId";
        String userName = "test";
        Credential credential = new Credential("", "");
        try {
            GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .build();
        } catch (TencentCloudSDKException e) {
            Assert.assertEquals(CamErrorCode.RESOURCENOTFOUND_SECRETNOTEXIST.getValue(), e.getErrorCode());
        }
    }
}