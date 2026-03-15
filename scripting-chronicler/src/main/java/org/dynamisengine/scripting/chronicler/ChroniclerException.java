package org.dynamisengine.scripting.chronicler;

import org.dynamisengine.core.exception.DynamisException;

public final class ChroniclerException extends DynamisException {
    public ChroniclerException(String operation, String reason) {
        super("Chronicler operation '" + operation + "' failed: " + reason);
    }
}
