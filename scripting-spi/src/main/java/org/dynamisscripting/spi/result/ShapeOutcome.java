package org.dynamisscripting.spi.result;

import org.dynamisscripting.api.value.Intent;

public record ShapeOutcome(boolean shaped, Intent result, String reasonCode) {
}
