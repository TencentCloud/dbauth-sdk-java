Language : 🇺🇸 | [🇨🇳](./README.zh-CN.md)
<h1 align="center">Tencent Cloud DBAuth SDK</h1>
<div align="center">
Welcome to the Tencent Cloud DBAuth SDK, which provides developers with supporting development tools to access the Tencent Cloud Database CAM verification service, simplifying the access process of the Tencent Cloud Database CAM verification service.
</div>

### Dependency Environment

1. Dependency Environment: JDK version 1.8 and above.
2. Before use, CAM verification must be enabled on the Tencent Cloud console.
3. On the Tencent Cloud console, view the account APPID on
   the [account information](https://console.cloud.tencent.com/developer) page, and obtain the SecretID and SecretKey on
   the [access management](https://console.cloud.tencent.com/cam/capi) page.

### USAGE

```
    <dependency>
      <groupId>com.tencentcloudapi</groupId>
      <artifactId>tencentcloud-dbauth-sdk-java</artifactId>
      <version>1.0.3</version>
    </dependency>
```

### Example - Generate CAM Authentication Token

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
            // Instantiate an HTTP profile, optional, can be skipped if there are no special requirements
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("cam.tencentcloudapi.com");
            // Instantiate a client profile, optional, can be skipped if there are no special requirements
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // Build a GenerateAuthenticationTokenRequest.
            GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                    .region(region)
                    .credential(credential)
                    .userName(userName)
                    .instanceId(instanceId)
                    .clientProfile(clientProfile) // clientProfile is optional
                    .build();

            return DBAuthentication.generateAuthenticationToken(tokenRequest);

        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
        return "";
    }
}
```

### Example - Connect to a Database Instance

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
        // Define the necessary variables for the connection
        String region = "ap-guangzhou";
        String instanceId = "cdb-123456";
        String userName = "test";
        String host = "gz-cdb-123456.sql.tencentcdb.com";
        int port = 3306;
        String dbName = "mysql";
        String secretId = System.getenv("TENCENTCLOUD_SECRET_ID");
        String secretKey = System.getenv("TENCENTCLOUD_SECRET_KEY");

        // Get the connection
        Connection connection = getDBConnectionUsingCAM(secretId, secretKey, region,
                instanceId, userName, host, port, dbName);

        // Verify the connection is successful
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT 'Success!';");
        while (rs.next()) {
            String id = rs.getString(1);
            System.out.println(id); // Should print "Success!"
        }

        // Close the connection
        stmt.close();
        connection.close();
    }

    /**
     * Get a database connection using CAM Database Authentication
     *
     * @param secretId   the secret ID
     * @param secretKey  the secret key
     * @param region     the region
     * @param instanceId the instance ID
     * @param userName   the username
     * @param host       the host
     * @param port       the port
     * @param dbName     the database name
     * @return a Connection object
     * @throws Exception if an error occurs
     */
    private static Connection getDBConnectionUsingCAM(
            String secretId, String secretKey, String region, String instanceId, String userName,
            String host, int port, String dbName) throws Exception {

        // Get the credentials from the secretId and secretKey
        Credential credential = new Credential(secretId, secretKey);

        // Define the maximum number of attempts
        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Get the authentication token using the credentials
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
     * Get an authentication token
     *
     * @param region     the region
     * @param instanceId the instance ID
     * @param userName   the username
     * @param credential the credential
     * @return an authentication token
     */
    private static String getAuthToken(String region, String instanceId, String userName, Credential credential) throws TencentCloudSDKException {
        // Instantiate an HTTP profile, optional, can be skipped if there are no special requirements
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("cam.tencentcloudapi.com");
        // Instantiate a client profile, optional, can be skipped if there are no special requirements
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        // Build a GenerateAuthenticationTokenRequest.
        GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                .region(region)
                .credential(credential)
                .userName(userName)
                .instanceId(instanceId)
                .clientProfile(clientProfile) // clientProfile is optional
                .build();

        return DBAuthentication.generateAuthenticationToken(tokenRequest);
    }
}
```

### Error Codes

Refer to the [error code document](https://cloud.tencent.com/document/product/598/33168) for more information.

### Limitations

There are some limitations when you use CAM database authentication. The following is from the CAM authentication
documentation.

When you use CAM database authentication, your application must generate an CAM authentication token. Your application
then uses that token to connect to the DB instance or cluster.

We recommend the following:

* Use CAM database authentication as a mechanism for temporary, personal access to databases.
* Use CAM database authentication only for workloads that can be easily retried.