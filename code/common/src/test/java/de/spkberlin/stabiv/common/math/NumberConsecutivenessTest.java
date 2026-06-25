package de.spkberlin.stabiv.common.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NumberConsecutivenessTest {

    @Test
    public void testNumberConsecutiveness() {
        assertTrue(NumberConsecutiveness.findGaps(Collections.emptyList(), 0).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(0), 0).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(0, 1), 0).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(0, 1, 2), 0).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(1, 2, 3), 1).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(0, 0), 0).isEmpty());
        assertTrue(NumberConsecutiveness.findGaps(List.of(1, 2), 2).isEmpty());

        assertEquals(List.of(1), NumberConsecutiveness.findGaps(List.of(0, 2), 0));
        assertEquals(List.of(0), NumberConsecutiveness.findGaps(List.of(1, 2), 0));
        assertEquals(List.of(1, 2), NumberConsecutiveness.findGaps(List.of(0, 3), 0));
        assertEquals(List.of(0, 1, 2), NumberConsecutiveness.findGaps(List.of(3), 0));
    }
}
