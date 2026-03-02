package org.dynamisscripting.chronicler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.WorldEvent;
import org.dynamisscripting.spi.ChroniclerNodeArchetype;

public final class ArchetypeInstantiator {
    private static final DynamisLogger LOGGER = DynamisLogger.get(ArchetypeInstantiator.class);

    private final Map<String, ChroniclerNodeArchetype> archetypes;

    public ArchetypeInstantiator(List<ChroniclerNodeArchetype> archetypes) {
        this.archetypes = new LinkedHashMap<>();
        if (archetypes != null) {
            for (ChroniclerNodeArchetype archetype : archetypes) {
                registerArchetype(archetype);
            }
        }
    }

    public void registerArchetype(ChroniclerNodeArchetype archetype) {
        if (archetype == null) {
            throw new ChroniclerException("registerArchetype", "archetype must not be null");
        }
        if (this.archetypes.containsKey(archetype.archetypeId())) {
            throw new ChroniclerException("registerArchetype", "duplicate archetypeId: " + archetype.archetypeId());
        }
        this.archetypes.put(archetype.archetypeId(), archetype);
    }

    public Optional<WorldEvent> tryInstantiate(
            ChroniclerNodeArchetype archetype,
            CanonLog canonLog,
            CanonTime currentTime) {
        try {
            if (!archetype.canInstantiate(canonLog, currentTime)) {
                return Optional.empty();
            }
            return Optional.ofNullable(archetype.instantiate(canonLog, currentTime));
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to instantiate archetype " + archetype.archetypeId(), exception);
            return Optional.empty();
        }
    }

    public List<WorldEvent> evaluateAll(CanonLog canonLog, CanonTime currentTime) {
        List<WorldEvent> events = new ArrayList<>();
        for (ChroniclerNodeArchetype archetype : archetypes.values()) {
            Optional<WorldEvent> worldEvent = tryInstantiate(archetype, canonLog, currentTime);
            worldEvent.ifPresent(events::add);
        }
        return List.copyOf(events);
    }
}
