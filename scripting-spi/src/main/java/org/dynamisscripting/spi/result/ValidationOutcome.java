package org.dynamisscripting.spi.result;

public record ValidationOutcome(boolean passed, String reasonCode, String explanation) {
}
