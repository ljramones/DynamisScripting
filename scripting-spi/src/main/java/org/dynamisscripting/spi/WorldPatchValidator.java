package org.dynamisscripting.spi;

import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.WorldPatch;
import org.dynamisscripting.spi.result.PatchValidationResult;

public interface WorldPatchValidator {
    String validatorId();

    PatchValidationResult validate(WorldPatch patch, CanonLog canonLog);
}
