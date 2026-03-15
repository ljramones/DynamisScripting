package org.dynamisengine.scripting.percept;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PerceptContributionRegistry {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> contributions;

    public PerceptContributionRegistry() {
        this.contributions = new ConcurrentHashMap<>();
    }

    public void register(String moduleId, String contributionType, Object contributionData) {
        validateNonBlank(moduleId, "moduleId");
        validateNonBlank(contributionType, "contributionType");
        if (contributionData == null) {
            throw new PerceptException("register", "contributionData must not be null");
        }
        contributions
                .computeIfAbsent(moduleId, ignored -> new ConcurrentHashMap<>())
                .put(contributionType, contributionData);
    }

    public Optional<Object> getContribution(String moduleId, String contributionType) {
        validateNonBlank(moduleId, "moduleId");
        validateNonBlank(contributionType, "contributionType");
        Map<String, Object> moduleContributions = contributions.get(moduleId);
        if (moduleContributions == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(moduleContributions.get(contributionType));
    }

    public Set<String> registeredModules() {
        return Set.copyOf(contributions.keySet());
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new PerceptException(field, "must not be null or blank");
        }
    }
}
