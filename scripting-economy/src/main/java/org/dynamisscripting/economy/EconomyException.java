package org.dynamisscripting.economy;

import org.dynamis.core.exception.DynamisException;

public final class EconomyException extends DynamisException {
    public EconomyException(String operation, String reason) {
        super("Economy operation '" + operation + "' failed: " + reason);
    }
}
