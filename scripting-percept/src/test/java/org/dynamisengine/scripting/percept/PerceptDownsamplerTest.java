package org.dynamisengine.scripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.scripting.api.value.Percept;
import org.junit.jupiter.api.Test;

class PerceptDownsamplerTest {
    @Test
    void belowThresholdReturnsAll() {
        PerceptDownsampler downsampler = new PerceptDownsampler();
        List<Percept> input = List.of(percept(0.4), percept(0.9));

        assertEquals(2, downsampler.downsample(input, 5).size());
    }

    @Test
    void aboveThresholdResultSizeAtMostMaxToDeliver() {
        PerceptDownsampler downsampler = new PerceptDownsampler();
        List<Percept> input = List.of(percept(0.2), percept(0.3), percept(0.4), percept(0.9));

        assertTrue(downsampler.downsample(input, 2).size() <= 2);
    }

    @Test
    void highFidelityPerceptsPrioritized() {
        PerceptDownsampler downsampler = new PerceptDownsampler();
        List<Percept> input = List.of(percept(0.2), percept(0.8), percept(0.9), percept(0.4));

        List<Percept> output = downsampler.downsample(input, 2);

        assertTrue(output.stream().allMatch(percept -> percept.fidelity() >= 0.75D));
    }

    @Test
    void isEventStormTrueWhenCountGreaterThanThreshold() {
        PerceptDownsampler downsampler = new PerceptDownsampler();
        assertTrue(downsampler.isEventStorm(101, 100));
        assertTrue(!downsampler.isEventStorm(100, 100));
    }

    private static Percept percept(double fidelity) {
        return Percept.of(EntityId.of(1L), "collision", "payload", fidelity, 1L);
    }
}
