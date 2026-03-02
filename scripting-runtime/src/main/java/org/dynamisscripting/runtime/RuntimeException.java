package org.dynamisscripting.runtime;

import org.dynamis.core.exception.DynamisException;

// Note: extends DynamisException (unchecked), not java.lang.RuntimeException directly.
public final class RuntimeException extends DynamisException {
    public RuntimeException(String operation, String reason) {
        super("Runtime operation '" + operation + "' failed: " + reason);
    }
}
