package org.dynamisengine.scripting.dsl;

import org.dynamisengine.core.exception.DynamisException;

public record ClauseTrace(String clauseText, Object resolvedValue, boolean passed, String explanation) {
    public ClauseTrace {
        if (clauseText == null) {
            throw new DynamisException("clauseText must not be null");
        }
        if (explanation == null) {
            throw new DynamisException("explanation must not be null");
        }
    }
}
