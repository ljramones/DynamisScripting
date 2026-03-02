package org.dynamisscripting.society;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SocietyRegistryTest {
    @Test
    void registerAndFindReturnsProfile() {
        SocietyRegistry registry = new SocietyRegistry();
        SocietyProfile profile = profile("empire");

        registry.register(profile);

        assertTrue(registry.find("empire").isPresent());
        assertEquals(profile, registry.find("empire").orElseThrow());
    }

    @Test
    void duplicateSocietyIdThrows() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire"));

        assertThrows(SocietyException.class, () -> registry.register(profile("empire")));
    }

    @Test
    void getUnknownThrows() {
        SocietyRegistry registry = new SocietyRegistry();
        assertThrows(SocietyException.class, () -> registry.get("unknown"));
    }

    @Test
    void unregisterRemovesProfile() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("empire"));

        registry.unregister("empire");

        assertTrue(registry.find("empire").isEmpty());
    }

    @Test
    void allReturnsImmutableList() {
        SocietyRegistry registry = new SocietyRegistry();
        registry.register(profile("one"));
        registry.register(profile("two"));

        assertEquals(2, registry.all().size());
        assertThrows(UnsupportedOperationException.class, () -> registry.all().add(profile("three")));
    }

    private static SocietyProfile profile(String id) {
        return SocietyProfile.of(
                id,
                id,
                Map.of("honor", DimensionWeight.of("honor", 0.7D, Map.of())),
                Map.of("survival", 0.4D));
    }
}
