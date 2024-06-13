语言 : [🇺🇸](./README.md) | 🇨🇳
<h1 align="center">Tencent Cloud DBAuth SDK</h1>
<div align="center">
欢迎使用腾讯云数据库CAM验证SDK，该SDK为开发者提供了支持的开发工具，以访问腾讯云数据库CAM验证服务，简化了腾讯云数据库CAM验证服务的接入过程。
</div>

### 依赖环境
1. 依赖环境: JDK 1.8版本及以上。
2. 使用前需要在腾讯云控制台启用CAM验证。
3. 在腾讯云控制台[账号信息](https://console.cloud.tencent.com/developer)页面查看账号APPID，[访问管理](https://console.cloud.tencent.com/cam/capi)页面获取 SecretID 和 SecretKey 。

### 使用

```
    <dependency>
      <groupId>com.tencentcloudapi</groupId>
      <artifactId>tencentcloud-dbauth-sdk-java</artifactId>
      <version>1.0.0</version>
    </dependency>
```

### 示例

```
package com.tencentcloud.dbauth;
import com.tencentcloudapi.common.Credential;
import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

public class GenerateDBAuthentication {

    public static void main(String[] args) {
        // 定义认证令牌的参数
        String region = "ap-guangzhou";
        String instanceId = "instanceId";
        String userName = "userName";
        // 从环境变量中获取凭证
        Credential credential = new Credential(System.getenv("TENCENTCLOUD_SECRET_ID"), System.getenv("TENCENTCLOUD_SECRET_KEY"));

        System.out.println(getAuthToken(region, instanceId, userName, credential));
    }

    public static String getAuthToken(String region, String instanceId, String userName, Credential credential) {
        try {
            // 构建 GenerateAuthenticationTokenRequest
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

[错误码](https://cloud.tencent.com/document/api/1312/48205#.E5.85.AC.E5.85.B1.E9.94.99.E8.AF.AF.E7.A0.81)

### 局限性

使用 CAM 数据库身份验证时存在一些限制。以下内容来自 CAM
身份验证文档。

当您使用 CAM 数据库身份验证时，您的应用程序必须生成 CAM 身份验证令牌。然后，您的应用程序使用该令牌连接到数据库实例或集群。

我们建议如下：

* 使用 CAM 数据库身份验证作为临时、个人访问数据库的机制。
* 仅对可以轻松重试的工作负载使用 CAM 数据库身份验证。