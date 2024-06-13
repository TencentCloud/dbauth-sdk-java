Language : 🇺🇸 | [🇨🇳](./README.zh-CN.md)
<h1 align="center">Tencent Cloud DBAuth SDK</h1>
<div align="center">
Welcome to the Tencent Cloud DBAuth SDK, which provides developers with supporting development tools to access the Tencent Cloud Database CAM verification service, simplifying the access process of the Tencent Cloud Database CAM verification service.
</div>

### Dependency Environment
1. Dependency Environment: JDK version 1.8 and above.
2. Before use, CAM verification must be enabled on the Tencent Cloud console.
3. On the Tencent Cloud console, view the account APPID on the [account information](https://console.cloud.tencent.com/developer) page, and obtain the SecretID and SecretKey on the [access management](https://console.cloud.tencent.com/cam/capi) page.

### USAGE

```
    <dependency>
      <groupId>com.tencentcloudapi</groupId>
      <artifactId>tencentcloud-dbauth-sdk-java</artifactId>
      <version>1.0.0</version>
    </dependency>
```

### Demo

```
package com.tencentcloud.dbauth;
import com.tencentcloudapi.common.Credential;
import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

public class GenerateDBAuthentication {

    public static void main(String[] args) {
        // Define the parameters for the authentication token.
        String region = "ap-guangzhou";
        String instanceId = "instanceId";
        String userName = "userName";
        // Get the credentials from the environment variables.
        Credential credential = new Credential(System.getenv("TENCENTCLOUD_SECRET_ID"), System.getenv("TENCENTCLOUD_SECRET_KEY"));

        System.out.println(getAuthToken(region, instanceId, userName, credential));
    }

    public static String getAuthToken(String region, String instanceId, String userName, Credential credential) {
        try {
            // Build a GenerateAuthenticationTokenRequest.
            GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .build();

            return DBAuthentication.generateAuthenticationToken(tokenRequest);

        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
        return "";
    }
}
```

[ErrorCodes](https://cloud.tencent.com/document/api/1312/48205#.E5.85.AC.E5.85.B1.E9.94.99.E8.AF.AF.E7.A0.81)

### Limitations

There are some limitations when you use CAM database authentication. The following is from the CAM authentication
documentation.

When you use CAM database authentication, your application must generate an CAM authentication token. Your application
then uses that token to connect to the DB instance or cluster.

We recommend the following:

* Use CAM database authentication as a mechanism for temporary, personal access to databases.
* Use CAM database authentication only for workloads that can be easily retried.