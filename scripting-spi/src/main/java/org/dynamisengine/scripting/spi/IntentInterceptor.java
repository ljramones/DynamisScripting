package org.dynamisengine.scripting.spi;

import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.WorldOracle.CommitResult;
import org.dynamisengine.scripting.api.WorldOracle.ValidationResult;
import org.dynamisengine.scripting.api.value.Intent;

public interface IntentInterceptor {
    String interceptorId();

    int priority();

    Intent beforeValidate(Intent intent);

    void afterCommit(Intent originalIntent, CommitResult result, CanonLog canonLog);

    default void afterReject(Intent intent, ValidationResult result) {
    }
}
