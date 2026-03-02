package org.dynamisscripting.oracle;

import org.dynamis.core.exception.DynamisException;

public final class OracleException extends DynamisException {
    public OracleException(String phase, String reason) {
        super("Oracle pipeline failed in phase '" + phase + "': " + reason);
    }
}
