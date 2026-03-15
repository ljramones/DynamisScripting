package org.dynamisengine.scripting.spi.result;

import org.dynamisengine.scripting.api.value.Intent;

public record ShapeOutcome(boolean shaped, Intent result, String reasonCode) {
}
