package org.dynamisengine.scripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PerceptDelayTest {
    @Test
    void soundDelayProportionalToDistance() {
        PerceptDelay delay = new PerceptDelay();
        assertEquals(1L, delay.computeDelayTicks(340.0D, "sound"));
    }

    @Test
    void lightDelayAlwaysZero() {
        PerceptDelay delay = new PerceptDelay();
        assertEquals(0L, delay.computeDelayTicks(1000.0D, "light"));
    }

    @Test
    void courierDelayProportionalToDistance() {
        PerceptDelay delay = new PerceptDelay();
        assertEquals(10L, delay.computeDelayTicks(100.0D, "courier"));
    }

    @Test
    void unknownMediumReturnsZero() {
        PerceptDelay delay = new PerceptDelay();
        assertEquals(0L, delay.computeDelayTicks(100.0D, "unknown"));
    }

    @Test
    void pureFunctionSameInputSameOutput() {
        PerceptDelay delay = new PerceptDelay();
        assertEquals(delay.computeDelayTicks(123.0D, "sound"), delay.computeDelayTicks(123.0D, "sound"));
    }
}
