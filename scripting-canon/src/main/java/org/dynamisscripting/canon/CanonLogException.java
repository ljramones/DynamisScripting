package org.dynamisscripting.canon;

import org.dynamis.core.exception.DynamisException;

public final class CanonLogException extends DynamisException {
    public CanonLogException(String operation, String reason) {
        super("CanonLog operation '" + operation + "' failed: " + reason);
    }
}
