package org.dynamisscripting.society;

import org.dynamis.core.exception.DynamisException;

public final class SocietyException extends DynamisException {
    public SocietyException(String operation, String reason) {
        super("Society operation '" + operation + "' failed: " + reason);
    }
}
