package de.spkberlin.imagevalidation.common.math;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

public class NumberConsecutivenessTest {

    @Test
    public void testNumberConsecutiveness() {
        assertNull(NumberConsecutiveness.check(List.of(0), 0));
        assertNull(NumberConsecutiveness.check(List.of(0, 1), 0));
        assertNull(NumberConsecutiveness.check(List.of(0, 1, 2), 0));
        assertNull(NumberConsecutiveness.check(List.of(1, 2, 3), 1));

        assertNotNull(NumberConsecutiveness.check(List.of(0, 0), 0));
        assertNotNull(NumberConsecutiveness.check(List.of(0, 2), 0));
        assertNotNull(NumberConsecutiveness.check(List.of(1, 2), 0));
        assertNotNull(NumberConsecutiveness.check(List.of(1, 2), 2));
    }
}
