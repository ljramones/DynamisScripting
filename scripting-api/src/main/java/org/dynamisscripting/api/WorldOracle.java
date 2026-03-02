package org.dynamisscripting.api;

import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.WorldEvent;

public interface WorldOracle {
    ValidationResult validate(Intent intent);

    ShapeResult shape(Intent intent);

    CommitResult commit(Intent intent);

    CommitResult commitWorldEvent(WorldEvent worldEvent);

    record ValidationResult(boolean valid, String reasonCode, String explanation) {
    }

    record ShapeResult(boolean shaped, Intent originalIntent, Intent shapedIntent, String reasonCode) {
    }

    record CommitResult(boolean committed, long commitId, String reasonCode) {
    }
}
