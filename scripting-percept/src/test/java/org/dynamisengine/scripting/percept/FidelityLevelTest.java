package org.dynamisengine.scripting.percept;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FidelityLevelTest {
    @Test
    void fromDoubleOneIsFull() {
        assertEquals(FidelityLevel.FULL, FidelityLevel.fromDouble(1.0D));
    }

    @Test
    void fromDoublePointSevenFiveIsHigh() {
        assertEquals(FidelityLevel.HIGH, FidelityLevel.fromDouble(0.75D));
    }

    @Test
    void fromDoublePointFourIsLow() {
        assertEquals(FidelityLevel.LOW, FidelityLevel.fromDouble(0.4D));
    }

    @Test
    void fromDoubleZeroIsTrace() {
        assertEquals(FidelityLevel.TRACE, FidelityLevel.fromDouble(0.0D));
    }
}
