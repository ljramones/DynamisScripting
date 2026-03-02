package org.dynamisscripting.society;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SocietyRegistry {
    private final ConcurrentHashMap<String, SocietyProfile> profiles;

    public SocietyRegistry() {
        this.profiles = new ConcurrentHashMap<>();
    }

    public void register(SocietyProfile profile) {
        if (profile == null) {
            throw new SocietyException("register", "profile must not be null");
        }
        SocietyProfile existing = profiles.putIfAbsent(profile.societyId(), profile);
        if (existing != null) {
            throw new SocietyException("register", "duplicate societyId: " + profile.societyId());
        }
    }

    public void replace(SocietyProfile profile) {
        if (profile == null) {
            throw new SocietyException("replace", "profile must not be null");
        }
        profiles.put(profile.societyId(), profile);
    }

    public void unregister(String societyId) {
        if (societyId == null || societyId.isBlank()) {
            throw new SocietyException("unregister", "societyId must not be null or blank");
        }
        profiles.remove(societyId);
    }

    public Optional<SocietyProfile> find(String societyId) {
        if (societyId == null || societyId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(profiles.get(societyId));
    }

    public SocietyProfile get(String societyId) {
        if (societyId == null || societyId.isBlank()) {
            throw new SocietyException("get", "societyId must not be null or blank");
        }
        SocietyProfile profile = profiles.get(societyId);
        if (profile == null) {
            throw new SocietyException("get", "unknown societyId: " + societyId);
        }
        return profile;
    }

    public List<SocietyProfile> all() {
        return List.copyOf(profiles.values());
    }

    public int size() {
        return profiles.size();
    }
}
