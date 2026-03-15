package org.dynamisengine.scripting.api.value;

import org.dynamisengine.core.exception.DynamisException;

public record CanonEvent(long commitId, CanonTime canonTime, String causalLink, Object delta) {
    public CanonEvent {
        if (commitId <= 0L) {
            throw new DynamisException("commitId must be greater than 0: " + commitId);
        }
        requireNonNull(canonTime, "canonTime");
        requireNonBlank(causalLink, "causalLink");
        requireNonNull(delta, "delta");
    }

    public static CanonEvent of(long commitId, CanonTime canonTime, String causalLink, Object delta) {
        return new CanonEvent(commitId, canonTime, causalLink, delta);
    }

    public boolean isAfter(CanonEvent other) {
        CanonEvent safeOther = requireNonNull(other, "other");
        return commitId > safeOther.commitId;
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DynamisException(field + " must not be null or blank");
        }
        return value;
    }
}
