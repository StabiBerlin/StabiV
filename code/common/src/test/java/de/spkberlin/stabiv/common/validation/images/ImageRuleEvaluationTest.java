package de.spkberlin.stabiv.common.validation.images;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.spkberlin.stabiv.common.validation.images.ImageRuleEvaluator;
import de.spkberlin.stabiv.common.validation.images.state.ImageRuleSimpleConditionOperatorType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ImageRuleEvaluationTest {

    /**
     * Test matches condition.
     */
    @Test
    public void testMatchesCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateMatchesCondition(List.of("^test$"), "test"));
        assertNull(ImageRuleEvaluator.evaluateMatchesCondition(List.of("^test$"), List.of("test")));
        assertNull(ImageRuleEvaluator.evaluateMatchesCondition(List.of("^test$", "^other$"), "other"));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateMatchesCondition(Collections.emptyList(), "blah"));
        assertNotNull(ImageRuleEvaluator.evaluateMatchesCondition(List.of("^test$"), null));
        assertNotNull(ImageRuleEvaluator.evaluateMatchesCondition(List.of("^test$"), "blah"));
    }

    /**
     * Test equal condition.
     */
    @Test
    public void testEqualCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateEqualCondition(List.of("test"), "test"));
        assertNull(ImageRuleEvaluator.evaluateEqualCondition(List.of("test"), List.of("test")));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateEqualCondition(Collections.emptyList(), "blah"));
        assertNotNull(ImageRuleEvaluator.evaluateEqualCondition(List.of("test"), null));
        assertNotNull(ImageRuleEvaluator.evaluateEqualCondition(List.of("test"), "blah"));
        assertNotNull(ImageRuleEvaluator.evaluateEqualCondition(List.of("test", "other"), "blah"));
    }

    /**
     * Test equals property condition.
     */
    @Test
    public void testEqualsPropertyCondition() {
        // context
        Map<String, String> context = Map.of("property", "value");

        // success
        assertNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.property"), "value", context));
        assertNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.property"), List.of("value"), context));
        assertNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$..property"), "value", context));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(Collections.emptyList(), "value", context));
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.property"), "other", context));
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.something"), "value", context));
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.something", "$.other"), "value", context));
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("something"), "value", context));
        assertNotNull(ImageRuleEvaluator.evaluateEqualsPropertyCondition(List.of("$.property"), null, context));
    }

    /**
     * Test exists condition.
     */
    @Test
    public void testExistsCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateExistsCondition(Collections.emptyList(), "test"));
        assertNull(ImageRuleEvaluator.evaluateExistsCondition(null, "test"));
        assertNull(ImageRuleEvaluator.evaluateExistsCondition(List.of("ignored"), "test"));
        assertNull(ImageRuleEvaluator.evaluateExistsCondition(List.of("ignored"), 123));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateExistsCondition(Collections.emptyList(), null));
        assertNotNull(ImageRuleEvaluator.evaluateExistsCondition(Collections.emptyList(), Collections.emptyList()));
    }

    /**
     * Test isEmpty condition.
     */
    @Test
    public void testIsEmptyCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), ""));
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(null, ""));
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(List.of("ignored"), ""));
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), Collections.emptyList()));
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), List.of("")));
        assertNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), null));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), "test"));
        assertNotNull(ImageRuleEvaluator.evaluateIsEmptyCondition(Collections.emptyList(), List.of("test")));
        assertNotNull(ImageRuleEvaluator.evaluateIsEmptyCondition(List.of("ignored"), "test"));
        assertNotNull(ImageRuleEvaluator.evaluateIsEmptyCondition(List.of("ignored"), 123));
    }

    /**
     * Test oneOf condition.
     */
    @Test
    public void testOneOfCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateOneOfCondition(List.of("one", "two"), "one"));
        assertNull(ImageRuleEvaluator.evaluateOneOfCondition(List.of("one", "two"), "two"));
        assertNull(ImageRuleEvaluator.evaluateOneOfCondition(List.of("one", "two"), List.of("one")));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateOneOfCondition(null, "one"));
        assertNotNull(ImageRuleEvaluator.evaluateOneOfCondition(Collections.emptyList(), null));
        assertNotNull(ImageRuleEvaluator.evaluateOneOfCondition(Collections.emptyList(), "one"));
        assertNotNull(ImageRuleEvaluator.evaluateOneOfCondition(List.of("one", "two"), "three"));
        assertNotNull(ImageRuleEvaluator.evaluateOneOfCondition(List.of("one", "two"), null));
    }

    /**
     * Test isInteger condition.
     */
    @Test
    public void testIsIntegerCondition() {
        // success
        assertNull(ImageRuleEvaluator.evaluateIsIntegerCondition(List.of("ignored"), "1"));
        assertNull(ImageRuleEvaluator.evaluateIsIntegerCondition(null, "-1"));
        assertNull(ImageRuleEvaluator.evaluateIsIntegerCondition(Collections.emptyList(), "0"));
        assertNull(ImageRuleEvaluator.evaluateIsIntegerCondition(List.of("ignored"), List.of("5")));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(Collections.emptyList(), null));
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(List.of("ignored"), "one"));
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(null, "two"));
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(null, Collections.emptyList()));
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(null, List.of("three")));
        assertNotNull(ImageRuleEvaluator.evaluateIsIntegerCondition(null, "3.1416"));
    }

    /**
     * Test number condition.
     */
    @Test
    public void testNumberCondition() {
        final ImageRuleSimpleConditionOperatorType lowerThan = ImageRuleSimpleConditionOperatorType.lowerThan;
        final ImageRuleSimpleConditionOperatorType lowerThanOrEqual = ImageRuleSimpleConditionOperatorType.lowerThanOrEqual;
        final ImageRuleSimpleConditionOperatorType greaterThan = ImageRuleSimpleConditionOperatorType.greaterThan;
        final ImageRuleSimpleConditionOperatorType greaterThanOrEqual = ImageRuleSimpleConditionOperatorType.greaterThanOrEqual;

        // success
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), lowerThan, "1.0"));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), lowerThan, List.of("1.0")));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("1.0"), lowerThanOrEqual, "1.0"));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), lowerThanOrEqual, "1.0"));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("1.0"), greaterThan, "2.0"));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), greaterThanOrEqual, "2.0"));
        assertNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("1.0"), greaterThanOrEqual, "2.0"));

        // failed
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(null, lowerThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(Collections.emptyList(), lowerThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("1.0"), null, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("1.0"), lowerThan, null));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("0.0"), lowerThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("0.0"), lowerThanOrEqual, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), greaterThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), greaterThanOrEqual, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0", "3.0"), lowerThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), lowerThan, "blub"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("blub"), lowerThan, "1.0"));
        assertNotNull(ImageRuleEvaluator.evaluateNumberCondition(List.of("2.0"), ImageRuleSimpleConditionOperatorType.isEmpty, "1.0"));
    }
}
