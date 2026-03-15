package org.dynamisengine.scripting.spi;

import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.value.WorldPatch;
import org.dynamisengine.scripting.spi.result.PatchValidationResult;

public interface WorldPatchValidator {
    String validatorId();

    PatchValidationResult validate(WorldPatch patch, CanonLog canonLog);
}
