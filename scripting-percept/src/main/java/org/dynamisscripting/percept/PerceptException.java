package org.dynamisscripting.percept;

import org.dynamis.core.exception.DynamisException;

public final class PerceptException extends DynamisException {
    public PerceptException(String operation, String reason) {
        super("PerceptBus operation '" + operation + "' failed: " + reason);
    }
}
