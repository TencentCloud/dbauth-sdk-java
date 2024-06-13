package com.tencentcloud.dbauth.internal;

import com.tencentcloudapi.cam.v20190116.CamErrorCode;
import org.apache.commons.lang3.StringUtils;

public final class ErrorCodeMatcher {
    public static final String ERROR_AUTH_FAILURE_PREFIX = "AuthFailure.";

    /**
     * Check if the error code requires user notification.
     *
     * @param errorCode the error code
     * @return true if the error code requires user notification, false otherwise
     */
    public static boolean isUserNotificationRequired(String errorCode) {
        if (StringUtils.isEmpty(errorCode)) {
            return false;
        }

        // ignoring case considerations
        return errorCode.toLowerCase().startsWith(ERROR_AUTH_FAILURE_PREFIX.toLowerCase())
                || errorCode.equalsIgnoreCase(CamErrorCode.RESOURCENOTFOUND_DATAFLOWAUTHCLOSE.getValue());
    }
}