package org.dynamisengine.scripting.oracle;

import org.dynamisengine.core.exception.DynamisException;

public final class OracleException extends DynamisException {
    public OracleException(String phase, String reason) {
        super("Oracle pipeline failed in phase '" + phase + "': " + reason);
    }
}
