package org.dynamisengine.scripting.spi.result;

public record ValidationOutcome(boolean passed, String reasonCode, String explanation) {
}
