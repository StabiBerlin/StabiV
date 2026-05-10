package de.spkberlin.imagevalidation.common.validation.images.rules;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRule;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleCompositeAndCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleCompositeCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleCompositeNotCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleCompositeOrCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleSimpleCondition;
import de.spkberlin.imagevalidation.common.validation.images.rules.state.ImageRuleSimpleConditionOperatorType;
import de.spkberlin.imagevalidation.common.validation.state.ValidationFailureSeverity;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageRuleEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ImageRuleEvaluator.class);

    private static final String DEFAULT_CONTEXT = "$";

    /**
     * Evaluate a list of image rules against a property object.
     *
     * @param rules the list of image rules
     * @param properties the property object extracted from the image
     * @return a list of result objects containing validation errors and warnings for each rule
     * @throws IOException in case rule evaluation fails
     */
    public static List<ValidationResult> evaluateRules(List<ImageRule> rules, Object properties) throws IOException {
        logger.trace("evaluate rules for the following properties:\n\n{}\n", PropertiesSerializer.serializeAsPrettyJson(properties));
        return rules
            .stream()
            .map(r -> evaluateRule(r, properties))
            .toList();
    }

    /**
     * Evaluate a single image rule against a property object.
     *
     * @param rule the image rule to check
     * @param properties the property object extracted from the image
     * @return a result object containing validation errors and warnings
     */
    public static ValidationResult evaluateRule(ImageRule rule, Object properties) {
        logger.trace("evalute rule '{}'", Objects.nonNull(rule.getMessage()) ? rule.getMessage() : rule.getSeverity());
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String context = Objects.nonNull(rule.getContext()) ? rule.getContext() : DEFAULT_CONTEXT;
        List<Object> contextObjects = resolveContext(context, properties);
        for (Object contextObject : contextObjects) {
            String conditionError = evaluateCondition(rule.getCondition(), contextObject);
            if (Objects.nonNull(conditionError)) {
                if (ValidationFailureSeverity.error.equals(rule.getSeverity())) {
                    logger.debug("validation error '{}'", rule.getMessage());
                    logger.trace("validation error details: {}", conditionError);
                    errors.add(rule.getMessage());
                } else if (ValidationFailureSeverity.warning.equals(rule.getSeverity())) {
                    logger.debug("validation warning: '{}'", rule.getMessage());
                    logger.trace("validation warning details: {}", conditionError);
                    warnings.add(rule.getMessage());
                }
            }
        }
        return ValidationResult.of(errors, warnings);
    }

    /**
     * Evaluate the "condition" part against a property object resolved from the "context" of an image rule.
     *
     * @param condition the image rule condition
     * @param contextProperties the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateCondition(ImageRuleCondition condition, Object contextProperties) {
        if (condition instanceof ImageRuleSimpleCondition) {
            ImageRuleSimpleCondition simpleCondition = (ImageRuleSimpleCondition) condition;
            Object propertyObject = null;
            try {
                propertyObject = JsonPath.read(contextProperties, simpleCondition.getProperty());
            } catch (PathNotFoundException e) {
                // do not return this as an error, because assert could be "not exist" and returning and error would violate not composite
                logger.trace(
                    "ignore that json path '{}' was not found in properties when evaluating condition",
                    simpleCondition.getProperty()
                );
            }
            return evaluateSimpleCondition(simpleCondition, propertyObject, contextProperties);
        }
        if (condition instanceof ImageRuleCompositeCondition) {
            ImageRuleCompositeCondition compositeCondition = (ImageRuleCompositeCondition) condition;
            return evaluateCompositeCondition(compositeCondition, contextProperties);
        }

        return String.format("rule condition '%s' not yet implemented", condition.getClass().getName());
    }

    /**
     * Evaluate a composite condition (and, or, not) against a property object resolved from the "context" of an image rule.
     *
     * @param condition the composite condition
     * @param properties the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateCompositeCondition(ImageRuleCompositeCondition condition, Object properties) {
        if (condition instanceof ImageRuleCompositeAndCondition) {
            return evaluateCompositeAndCondition((ImageRuleCompositeAndCondition) condition, properties);
        } else if (condition instanceof ImageRuleCompositeOrCondition) {
            return evaluateCompositeOrCondition((ImageRuleCompositeOrCondition) condition, properties);
        } else if (condition instanceof ImageRuleCompositeNotCondition) {
            return evaluateCompositeNotCondition((ImageRuleCompositeNotCondition) condition, properties);
        }
        return String.format("composite condition '%s' of unknown type", condition.getClass().getName());
    }

    /**
     * Evaluate an "and" composite condition against a property object resolved from the "context" of an image rule.
     *
     * @param condition the "and" composite condition
     * @param properties the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateCompositeAndCondition(ImageRuleCompositeAndCondition condition, Object properties) {
        // evaluate all sub conditions
        List<String> results = condition
            .getAnd()
            .stream()
            .map(c -> evaluateCondition(c, properties))
            .toList();

        List<String> failed = results
            .stream()
            .filter(r -> Objects.nonNull(r))
            .toList();

        if (failed.size() > 0) {
            return String.format("and condition failed with: %s", failed.stream().collect(Collectors.joining("; ")));
        }

        return null;
    }

    /**
     * Evaluate an "or" composite condition against a property object resolved from the "context" of an image rule.
     *
     * @param condition the "or" composite condition
     * @param properties the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateCompositeOrCondition(ImageRuleCompositeOrCondition condition, Object properties) {
        // evaluate all sub conditions
        List<String> results = condition
            .getOr()
            .stream()
            .map(c -> evaluateCondition(c, properties))
            .toList();

        if (!results.stream().anyMatch(r -> Objects.isNull(r))) {
            return String.format("no or alternative matched: " + results.getFirst());
        }

        return null;
    }

    /**
     * Evaluate a "not" composite condition against a property object resolved from the "context" of an image rule.
     *
     * @param condition the "not" composite condition
     * @param properties the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateCompositeNotCondition(ImageRuleCompositeNotCondition condition, Object properties) {
        // evaluate all sub conditions
        String result = evaluateCondition(condition.getNot(), properties);

        if (Objects.isNull(result)) {
            return String.format("'not' condition expected to fail");
        }

        return null;
    }

    /**
     * Evaluate a simple non-composite condition against a property object resolved from the "context" of an image rule.
     *
     * @param condition the simple non-composite condition
     * @param properties the resolved property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateSimpleCondition(ImageRuleSimpleCondition condition, Object properties, Object contextProperties) {
        switch (condition.getOperator()) {
            case ImageRuleSimpleConditionOperatorType.equal:
                return evaluateEqualCondition(condition.getValues(), properties);
            case ImageRuleSimpleConditionOperatorType.equalsProperty:
                return evaluateEqualsPropertyCondition(condition.getValues(), properties, contextProperties);
            case ImageRuleSimpleConditionOperatorType.lowerThan:
            case ImageRuleSimpleConditionOperatorType.lowerThanOrEqual:
            case ImageRuleSimpleConditionOperatorType.greaterThan:
            case ImageRuleSimpleConditionOperatorType.greaterThanOrEqual:
                return evaluateNumberCondition(condition.getValues(), condition.getOperator(), properties);
            case ImageRuleSimpleConditionOperatorType.isInteger:
                return evaluateIsIntegerCondition(condition.getValues(), properties);
            case ImageRuleSimpleConditionOperatorType.occurrences:
                return evaluateOccurrencesCondition(condition, properties);
            case ImageRuleSimpleConditionOperatorType.oneOf:
                return evaluateOneOfCondition(condition.getValues(), properties);
            case ImageRuleSimpleConditionOperatorType.isEmpty:
                return evaluateIsEmptyCondition(condition.getValues(), properties);
            case ImageRuleSimpleConditionOperatorType.exists:
                return evaluateExistsCondition(condition.getValues(), properties);
            case ImageRuleSimpleConditionOperatorType.matches:
                return evaluateMatchesCondition(condition.getValues(), properties);
            default:
                return String.format("failed validation with unknown operator '%s'", condition.getOperator());
        }
    }

    /**
     * Evaluate a "matches" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>A "matches" condition interprets the condition values as regular expressions and checks whether the extracted image property
     * matches any of the provided regular expressions.
     *
     * @param values the list of comparison values (must be at least of regular expression)
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateMatchesCondition(List<String> values, Object extractedValue) {
        if (Objects.isNull(values) || values.size() == 0) {
            return String.format("matches operator requires comparison values as regular expressions");
        }

        if (Objects.isNull(extractedValue)) {
            return "missing extracted value cannot match regular expression";
        }

        String extractedString = extractedValue.toString();

        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 1) {
                extractedString = extractedList.get(0).toString();
            }
        }

        for (String comparisonString : values) {
            if (extractedString.matches(comparisonString)) {
                return null;
            }
        }

        return String.format(
            "extracted value '%s' doesn't match regular expression %s",
            PropertiesSerializer.toPrettyString(extractedValue),
            values
                .stream()
                .map(s -> String.format("'%s'", s))
                .collect(Collectors.joining(", "))
        );
    }

    /**
     * Evaluate an "exists" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>An "exists" condition checks wether any (possibly empty) value was extracted from the image for this property.
     *
     * @param values the list of comparison values (is ignored)
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateExistsCondition(List<String> values, Object extractedValue) {
        if (Objects.nonNull(values) && values.size() > 0) {
            logger.warn("ignoring values of 'doesNotExist' condition");
        }

        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 0) {
                return String.format("extracted value '%s' does not exist", PropertiesSerializer.toPrettyString(extractedValue));
            }
            return null;
        }

        if (Objects.isNull(extractedValue)) {
            return String.format("extracted value '%s' does not exist", PropertiesSerializer.toPrettyString(extractedValue));
        }

        return null;
    }

    /**
     * Evaluate an "empty" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>An "empty" condition checks wether the extracted image property can be considered empty (when interpreted as a string).
     *
     * @param values the list of comparison values (is ignored)
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateIsEmptyCondition(List<String> values, Object extractedValue) {
        if (Objects.nonNull(values) && values.size() > 0) {
            logger.warn("ignoring values of 'isEmpty' condition");
        }

        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 0) {
                // element does not exist
                return null;
            } else if (extractedList.size() == 1 && extractedList.get(0) instanceof String) {
                String extractedString = (String) extractedList.get(0);
                if (!extractedString.isEmpty()) {
                    return String.format("extracted value '%s' is not empty", PropertiesSerializer.toPrettyString(extractedValue));
                }
                // unpack success
                return null;
            }
        }

        if (extractedValue instanceof String) {
            if (!((String) extractedValue).isEmpty()) {
                return String.format("extracted value '%s' is not empty", PropertiesSerializer.toPrettyString(extractedValue));
            }
            return null;
        }

        if (Objects.nonNull(extractedValue)) {
            return String.format("extracted value '%s' is not empty", PropertiesSerializer.toPrettyString(extractedValue));
        }

        return null;
    }

    /**
     * Evaluate a "oneOf" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>A "oneOf" condition checks wether the extracted image property is equal to one of the provided condition comparison values.
     *
     * @param values the list of comparison values
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateOneOfCondition(List<String> values, Object extractedValue) {
        if (Objects.isNull(values) || values.isEmpty()) {
            return String.format("oneOf operator requires at least one comparison value");
        }
        if (Objects.isNull(extractedValue)) {
            return "missing extracted value cannot match one of the provided comparison values";
        }
        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 1) {
                if (values.contains(extractedList.get(0).toString())) {
                    // unpack success
                    return null;
                }
            }
        }
        if (!values.contains(extractedValue.toString())) {
            return String.format("extracted value '%s' not one of '%s'", PropertiesSerializer.toPrettyString(extractedValue), values);
        }
        return null;
    }

    /**
     * Evaluate an "isInteger" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>An "isInteger" condition checks wether the extracted image property is can be interpreted as a natural number.
     *
     * @param values the list of comparison values (ignored)
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateIsIntegerCondition(List<String> values, Object extractedValue) {
        if (Objects.nonNull(values) && values.size() > 0) {
            logger.warn("ignoring values of 'isInteger' condition");
        }
        try {
            extractedValueToInteger(extractedValue);
        } catch (NumberFormatException e) {
            return String.format("could not parse extracted value '%s' as an integer", PropertiesSerializer.toPrettyString(extractedValue));
        }
        return null;
    }

    /**
     * Evaluate an "occurrences" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>An "occurrences" condition checks wether the extracted image property is a list with a length matching the condition value.
     *
     * @param condition the "occurrences" condition
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateOccurrencesCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (!(extractedValue instanceof List)) {
            return String.format(
                "cannot determine number of occurrences of extracted value '%s'",
                condition.getValues(),
                PropertiesSerializer.toPrettyString(extractedValue)
            );
        }
        Integer extractedLength = ((List<?>) extractedValue).size();
        Integer comparisonLength = condition.getValues().size();
        if (extractedLength != comparisonLength) {
            return String.format("extracted occurrences %d does not match expected occurrences %d", extractedLength, comparisonLength);
        }
        return null;
    }

    /**
     * Evaluate an "equal" condition against a property object resolved from the "context" of an image rule.
     *
     * <p>An "equal" condition checks wether the extracted image property is exactly equal to the provided condition value.
     *
     * @param values the list of comparison values (must be exactly one)
     * @param extractedValue the resolved property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateEqualCondition(List<String> values, Object extractedValue) {
        if (Objects.isNull(values) || values.size() == 0) {
            return String.format("equal operator requires comparison values");
        }
        if (values.size() > 1) {
            return String.format("too many comparison values for equal operator (use oneOf instead?)");
        }
        if (Objects.isNull(extractedValue)) {
            return "missing extracted value cannot equal provided comparison value";
        }

        String comparisonValue = values.get(0);

        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 1) {
                if (comparisonValue.equals(extractedList.get(0).toString())) {
                    // unpack success
                    return null;
                }
            }
        }

        if (!comparisonValue.equals(extractedValue.toString())) {
            return String.format(
                "extracted value '%s' does not equal comparison value '%s'",
                PropertiesSerializer.toPrettyString(extractedValue),
                values.get(0)
            );
        }
        return null;
    }

    /**
     * Evaluate an "equalsProperty" condition by extracting a comparison value using the json path provided as condition value.
     *
     * <p>An "equal" condition checks wether the extracted image property is exactly equal to the provided condition value.
     *
     * @param values the list of comparison values (must be exactly one JsonPath)
     * @param extractedValue the resolved property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateEqualsPropertyCondition(List<String> values, Object extractedValue, Object contextProperties) {
        if (Objects.isNull(values) || values.size() == 0) {
            return String.format("equalsProperty operator requires comparison property");
        }
        if (values.size() > 1) {
            return String.format("only one comparison property is allowed for equalsProperty operator");
        }
        if (Objects.isNull(extractedValue)) {
            return "missing extracted value cannot equal provided comparison property";
        }

        // extract comparison value based on provided json path
        String comparisonPath = values.get(0);
        Object comparisonValue = null;
        try {
            comparisonValue = JsonPath.read(contextProperties, comparisonPath);
            if (comparisonValue instanceof List) {
                List<?> comparisonList = (List<?>) comparisonValue;
                if (comparisonList.size() == 1) {
                    comparisonValue = comparisonList.get(0);
                }
            }
        } catch (PathNotFoundException e) {
            return "comparison property is missing";
        }

        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 1) {
                if (comparisonValue.equals(extractedList.get(0).toString())) {
                    // unpack success
                    return null;
                }
            }
        }

        if (!extractedValue.equals(comparisonValue)) {
            return String.format(
                "extracted value '%s' does not equal comparison property with value '%s'",
                PropertiesSerializer.toPrettyString(extractedValue),
                PropertiesSerializer.toPrettyString(comparisonValue)
            );
        }

        return null;
    }

    /**
     * Evaluate a number comparison condition against a property object resolved from the "context" of an image rule.
     *
     * <p>A number comparison condition checks wether the extracted image property is can be interpreted as a number and fulfills the
     * comparison condition (lower than, lower than or equal, greater than, greater than or equal).
     *
     * @param values the list of comparison values (must be exactly one number)
     * @param operator the comparison operator
     * @param extractedValue the resolved context property object extracted from the image
     * @return an error message if the condition fails, or null
     */
    public static String evaluateNumberCondition(
        List<String> values,
        ImageRuleSimpleConditionOperatorType operator,
        Object extractedValue
    ) {
        if (Objects.isNull(values) || values.size() == 0) {
            return String.format("comparison value is required for condition operator '%s'", operator);
        }
        if (values.size() > 1) {
            return String.format("too many comparison values for condition operator '%s'", operator);
        }
        Float comparisonFloat;
        try {
            comparisonFloat = Float.valueOf(values.get(0));
        } catch (NumberFormatException e) {
            return String.format("could not parse comparison value '%s' as a number", PropertiesSerializer.toPrettyString(values.get(0)));
        }
        Float extractedFloat;
        try {
            extractedFloat = extractedValueToFloat(extractedValue);
        } catch (NumberFormatException e) {
            return String.format("could not parse extracted value '%s' as a number", PropertiesSerializer.toPrettyString(extractedValue));
        }
        if (Objects.isNull(operator)) {
            return String.format("failed validation with unknown number operator '%s'", operator);
        }
        switch (operator) {
            case ImageRuleSimpleConditionOperatorType.lowerThan:
                if (!(extractedFloat < comparisonFloat)) {
                    return String.format("value %f not lower than %f", extractedFloat, comparisonFloat);
                }
                break;
            case ImageRuleSimpleConditionOperatorType.lowerThanOrEqual:
                if (!(extractedFloat <= comparisonFloat)) {
                    return String.format("value %f not lower or equal than %f", extractedFloat, comparisonFloat);
                }
                break;
            case ImageRuleSimpleConditionOperatorType.greaterThan:
                if (!(extractedFloat > comparisonFloat)) {
                    return String.format("value %f not greater than %f", extractedFloat, comparisonFloat);
                }
                break;
            case ImageRuleSimpleConditionOperatorType.greaterThanOrEqual:
                if (!(extractedFloat >= comparisonFloat)) {
                    return String.format("value %f not greater or equal than %f", extractedFloat, comparisonFloat);
                }
                break;
            default:
                return String.format("failed validation with unknown number operator '%s'", operator);
        }
        // success
        return null;
    }

    private static Float extractedValueToFloat(Object extractedValue) throws NumberFormatException {
        if (extractedValue instanceof String) {
            return Float.valueOf((String) extractedValue);
        } else if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() != 1) {
                throw new NumberFormatException(
                    String.format(
                        "cannot convert multiple extracted values '%s' to number",
                        PropertiesSerializer.toPrettyString(extractedValue)
                    )
                );
            }
            if (extractedList.get(0) instanceof String) {
                return Float.valueOf((String) extractedList.get(0));
            }
        }
        throw new NumberFormatException(
            String.format("cannot convert extracted value '%s' to number", PropertiesSerializer.toPrettyString(extractedValue))
        );
    }

    private static Integer extractedValueToInteger(Object extractedValue) throws NumberFormatException {
        if (extractedValue instanceof String) {
            return Integer.valueOf((String) extractedValue);
        } else if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() != 1) {
                throw new NumberFormatException(
                    String.format(
                        "cannot convert multiple extracted values '%s' to number",
                        PropertiesSerializer.toPrettyString(extractedValue)
                    )
                );
            }
            if (extractedList.get(0) instanceof String) {
                return Integer.valueOf((String) extractedList.get(0));
            }
        }
        throw new NumberFormatException(
            String.format("cannot convert extracted value '%s' to number", PropertiesSerializer.toPrettyString(extractedValue))
        );
    }

    private static List<Object> resolveContext(String contextPath, Object properties) {
        logger.trace("resolve context jsonpath '{}'", contextPath);
        Object matches = Collections.emptyList();
        try {
            matches = JsonPath.read(properties, contextPath);
        } catch (PathNotFoundException e) {
            // ignore context that is not available
            logger.trace("ignore that json path '{}' was not found in properties when determining rule context", contextPath);
        }

        // make sure to return a list, even if only one element matched
        @SuppressWarnings("unchecked")
        List<Object> contextObjects = (matches instanceof List)
            ? (List<Object>) matches
            : Collections.singletonList(matches).stream().filter(Objects::nonNull).toList();

        logger.trace("found {} matching context objects", contextObjects.size());
        return contextObjects;
    }
}
