package org.dynamisengine.scripting.canon;

import org.dynamisengine.core.exception.DynamisException;

public final class CanonLogException extends DynamisException {
    public CanonLogException(String operation, String reason) {
        super("CanonLog operation '" + operation + "' failed: " + reason);
    }
}
