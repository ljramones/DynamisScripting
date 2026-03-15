package org.dynamisengine.scripting.percept;

import org.dynamisengine.core.exception.DynamisException;

public final class PerceptException extends DynamisException {
    public PerceptException(String operation, String reason) {
        super("PerceptBus operation '" + operation + "' failed: " + reason);
    }
}
