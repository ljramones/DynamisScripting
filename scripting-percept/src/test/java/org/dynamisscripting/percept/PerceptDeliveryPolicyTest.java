package org.dynamisscripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

class PerceptDeliveryPolicyTest {
    @Test
    void emptySetAcceptsAllTypes() {
        PerceptDeliveryPolicy policy = PerceptDeliveryPolicy.of(EntityId.of(1L), Set.of(), 0.0D, true);
        assertTrue(policy.acceptsType("collision"));
        assertTrue(policy.acceptsType("rumour"));
    }

    @Test
    void nonEmptySetAcceptsOnlyListedTypes() {
        PerceptDeliveryPolicy policy = PerceptDeliveryPolicy.of(EntityId.of(1L), Set.of("collision"), 0.0D, true);
        assertTrue(policy.acceptsType("collision"));
        assertFalse(policy.acceptsType("rumour"));
    }

    @Test
    void minimumFidelityAccessible() {
        PerceptDeliveryPolicy policy = PerceptDeliveryPolicy.of(EntityId.of(1L), Set.of(), 0.5D, true);
        assertEquals(0.5D, policy.minimumFidelity());
    }
}
