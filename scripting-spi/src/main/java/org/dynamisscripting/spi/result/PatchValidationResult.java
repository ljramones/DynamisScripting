package org.dynamisscripting.spi.result;

import java.util.List;

public record PatchValidationResult(boolean valid, List<String> errors, List<String> warnings) {
    public PatchValidationResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }
}
