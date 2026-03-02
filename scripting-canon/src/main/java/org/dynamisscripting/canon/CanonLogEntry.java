package org.dynamisscripting.canon;

import org.dynamis.core.exception.DynamisException;
import org.dynamisscripting.api.value.CanonEvent;

public record CanonLogEntry(CanonEvent event, long sequenceNumber, long wallNanosAtInsert) {
    public CanonLogEntry {
        if (event == null) {
            throw new DynamisException("event must not be null");
        }
        if (sequenceNumber <= 0L) {
            throw new DynamisException("sequenceNumber must be > 0: " + sequenceNumber);
        }
        if (wallNanosAtInsert < 0L) {
            throw new DynamisException("wallNanosAtInsert must be >= 0: " + wallNanosAtInsert);
        }
    }

    public static CanonLogEntry of(CanonEvent event, long sequenceNumber, long wallNanosAtInsert) {
        return new CanonLogEntry(event, sequenceNumber, wallNanosAtInsert);
    }
}
