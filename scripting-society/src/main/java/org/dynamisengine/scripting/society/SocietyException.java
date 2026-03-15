package org.dynamisengine.scripting.society;

import org.dynamisengine.core.exception.DynamisException;

public final class SocietyException extends DynamisException {
    public SocietyException(String operation, String reason) {
        super("Society operation '" + operation + "' failed: " + reason);
    }
}
