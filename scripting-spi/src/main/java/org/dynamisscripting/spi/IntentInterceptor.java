package org.dynamisscripting.spi;

import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.WorldOracle.CommitResult;
import org.dynamisscripting.api.WorldOracle.ValidationResult;
import org.dynamisscripting.api.value.Intent;

public interface IntentInterceptor {
    String interceptorId();

    int priority();

    Intent beforeValidate(Intent intent);

    void afterCommit(Intent originalIntent, CommitResult result, CanonLog canonLog);

    default void afterReject(Intent intent, ValidationResult result) {
    }
}
