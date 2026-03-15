package org.dynamisengine.scripting.api.value;

import java.util.List;
import java.util.regex.Pattern;
import org.dynamisengine.core.exception.DynamisException;

public record WorldPatch(String version, List<String> changedRules, List<String> changedSchedules, List<String> changedAssets) {
    private static final Pattern SEMVER_PATTERN =
            Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?$");

    public WorldPatch {
        requireSemver(version);
        requireNonNull(changedRules, "changedRules");
        changedRules = List.copyOf(changedRules);
        requireNonNull(changedSchedules, "changedSchedules");
        changedSchedules = List.copyOf(changedSchedules);
        requireNonNull(changedAssets, "changedAssets");
        changedAssets = List.copyOf(changedAssets);
    }

    public static WorldPatch of(
            String version,
            List<String> changedRules,
            List<String> changedSchedules,
            List<String> changedAssets) {
        return new WorldPatch(version, changedRules, changedSchedules, changedAssets);
    }

    public boolean isEmpty() {
        return changedRules.isEmpty() && changedSchedules.isEmpty() && changedAssets.isEmpty();
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new DynamisException(field + " must not be null");
        }
        return value;
    }

    private static void requireSemver(String version) {
        if (version == null || version.isBlank()) {
            throw new DynamisException("version must not be null or blank");
        }
        if (!SEMVER_PATTERN.matcher(version).matches()) {
            throw new DynamisException("version must be a semantic version string, got: " + version);
        }
    }
}
