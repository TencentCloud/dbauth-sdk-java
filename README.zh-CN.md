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
      <version>1.0.2</version>
    </dependency>
```

### 示例 - 生成 CAM 身份验证令牌

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

### 示例 - 连接到数据库实例
```
package com.tencentcloud.examples;

import com.tencentcloud.dbauth.DBAuthentication;
import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CAMDatabaseAuthenticationTester {
    public static void main(String[] args) throws Exception {
        // 定义连接所需的变量
        String region = "ap-guangzhou";
        String instanceId = "cdb-123456";
        String userName = "test";
        String host = "gz-cdb-123456.sql.tencentcdb.com";
        int port = 3306;
        String dbName = "mysql";
        String secretId = System.getenv("TENCENTCLOUD_SECRET_ID");
        String secretKey = System.getenv("TENCENTCLOUD_SECRET_KEY");

        // 获取连接
        Connection connection = getDBConnectionUsingCAM(secretId, secretKey, region,
                instanceId, userName, host, port, dbName);

        // 验证连接是否成功
        Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT 'Success!';");
        while (rs.next()) {
            String id = rs.getString(1);
            System.out.println(id); // 应打印 "Success!"
        }

        // 关闭连接
        stmt.close();
        connection.close();
    }

    /**
     * 使用 CAM 数据库认证获取数据库连接
     *
     * @param secretId   密钥 ID
     * @param secretKey  密钥
     * @param region     地区
     * @param instanceId 实例 ID
     * @param userName   用户名
     * @param host       主机
     * @param port       端口
     * @param dbName     数据库名称
     * @return Connection 对象
     * @throws Exception 异常
     */
    private static Connection getDBConnectionUsingCAM(
            String secretId, String secretKey, String region, String instanceId, String userName,
            String host, int port, String dbName) throws Exception {

        // 从 secretId 和 secretKey 获取凭证
        Credential credential = new Credential(secretId, secretKey);

        // 定义最大尝试次数
        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // 使用凭证获取认证令牌
                String authToken = getAuthToken(region, instanceId, userName, credential);

                String connectionUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
                return DriverManager.getConnection(connectionUrl, userName, authToken);
            } catch (Exception e) {
                lastException = e;
                System.out.println("Attempt " + attempt + " failed.");
            }
        }
        System.out.println("All attempts failed. error: " + lastException.getMessage());
        throw lastException;
    }

    /**
     * 获取认证令牌
     *
     * @param region     区域
     * @param instanceId 实例 ID
     * @param userName   用户名
     * @param credential 凭证
     * @return 认证令牌
     */
    private static String getAuthToken(String region, String instanceId, String userName, Credential credential) throws TencentCloudSDKException {
        // 构建 GenerateAuthenticationTokenRequest
        GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                .region(region)
                .credential(credential)
                .userName(userName)
                .instanceId(instanceId)
                .build();

        return DBAuthentication.generateAuthenticationToken(tokenRequest);
    }
}
```

### 错误码
参见 [错误码](https://cloud.tencent.com/document/product/598/33168)。

### 局限性

使用 CAM 数据库身份验证时存在一些限制。以下内容来自 CAM
身份验证文档。

当您使用 CAM 数据库身份验证时，您的应用程序必须生成 CAM 身份验证令牌。然后，您的应用程序使用该令牌连接到数据库实例或集群。

我们建议如下：

* 使用 CAM 数据库身份验证作为临时、个人访问数据库的机制。
* 仅对可以轻松重试的工作负载使用 CAM 数据库身份验证。