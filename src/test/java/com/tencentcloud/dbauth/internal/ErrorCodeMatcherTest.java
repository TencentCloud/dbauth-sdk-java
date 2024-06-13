package com.tencentcloud.dbauth.internal;

import com.tencentcloudapi.cam.v20190116.CamErrorCode;
import org.junit.Assert;
import org.junit.Test;

public class ErrorCodeMatcherTest {

    @Test
    public void testIsUserNotificationRequiredWithValidErrorCode() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                        CamErrorCode.RESOURCENOTFOUND_DATAFLOWAUTHCLOSE.getValue()
                )
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithLowerCaseErrorCode() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                        CamErrorCode.RESOURCENOTFOUND_DATAFLOWAUTHCLOSE.getValue().toLowerCase()
                )
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithUpperCaseErrorCode() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                        CamErrorCode.RESOURCENOTFOUND_DATAFLOWAUTHCLOSE.getValue().toUpperCase()
                )
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithAuthFailurePrefix() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                        ErrorCodeMatcher.ERROR_AUTH_FAILURE_PREFIX + "any"
                )
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithLowerCaseAuthFailurePrefix() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                (ErrorCodeMatcher.ERROR_AUTH_FAILURE_PREFIX + "any").toLowerCase())
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithUpperCaseAuthFailurePrefix() {
        Assert.assertTrue(ErrorCodeMatcher.isUserNotificationRequired(
                        (ErrorCodeMatcher.ERROR_AUTH_FAILURE_PREFIX + "any").toUpperCase()
                )
        );
    }

    @Test
    public void testIsUserNotificationRequiredWithNullErrorCode() {
        Assert.assertFalse(ErrorCodeMatcher.isUserNotificationRequired(null));
    }

    @Test
    public void testIsUserNotificationRequiredWithEmptyErrorCode() {
        Assert.assertFalse(ErrorCodeMatcher.isUserNotificationRequired(""));
    }

    @Test
    public void testIsUserNotificationRequiredWithInvalidErrorCode() {
        Assert.assertFalse(ErrorCodeMatcher.isUserNotificationRequired("test"));
    }

    @Test
    public void testIsUserNotificationRequiredWithErrorCodeAndDelimiter() {
        Assert.assertFalse(ErrorCodeMatcher.isUserNotificationRequired(
                        CamErrorCode.RESOURCENOTFOUND_DATAFLOWAUTHCLOSE.getValue() + Constants.DELIMITER
                )
        );
    }
}