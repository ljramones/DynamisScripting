package org.dynamisengine.scripting.economy;

import org.dynamisengine.core.exception.DynamisException;

public final class EconomyException extends DynamisException {
    public EconomyException(String operation, String reason) {
        super("Economy operation '" + operation + "' failed: " + reason);
    }
}
