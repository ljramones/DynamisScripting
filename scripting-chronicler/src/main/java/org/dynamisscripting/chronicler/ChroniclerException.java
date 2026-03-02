package org.dynamisscripting.chronicler;

import org.dynamis.core.exception.DynamisException;

public final class ChroniclerException extends DynamisException {
    public ChroniclerException(String operation, String reason) {
        super("Chronicler operation '" + operation + "' failed: " + reason);
    }
}
