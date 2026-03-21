package de.spkberlin.imagevalidation.common.rules.images;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleCompositeAndCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleCompositeCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleCompositeNotCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleCompositeOrCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSeverityType;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSimpleCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSimpleConditionOperatorType;
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

    public static List<String> evaluateRules(List<ImageRule> rules, Object properties) throws IOException {
        logger.trace("evaluate rules for the following properties:\n\n{}\n", PropertiesSerializer.serializeProperties(properties));
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (ImageRule rule : rules) {
            errors.addAll(evaluateRule(rule, properties));
        }

        return errors;
    }

    public static List<String> evaluateRule(ImageRule rule, Object properties) {
        logger.trace("evalute rule '{}'", Objects.nonNull(rule.getMessage()) ? rule.getMessage() : rule.getSeverity());
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String context = Objects.nonNull(rule.getContext()) ? rule.getContext() : DEFAULT_CONTEXT;
        List<Object> contextObjects = resolveContext(context, properties);
        for (Object contextObject : contextObjects) {
            String conditionError = evaluateCondition(rule.getCondition(), contextObject);
            if (Objects.nonNull(conditionError)) {
                if (ImageRuleSeverityType.error.equals(rule.getSeverity())) {
                    logger.error("validation error '{}'", rule.getMessage());
                    logger.debug("validation error details: {}", conditionError);
                    errors.add(String.format("%s (%s)", rule.getMessage(), conditionError));
                } else if (ImageRuleSeverityType.error.equals(rule.getSeverity())) {
                    logger.warn("validation warning: '{}'", rule.getMessage());
                    logger.debug("validation warning details: {}", conditionError);
                    warnings.add(String.format("%s (%s)", rule.getMessage(), conditionError));
                }
            }
        }
        return errors;
    }

    public static String evaluateCondition(ImageRuleCondition condition, Object properties) {
        if (condition instanceof ImageRuleSimpleCondition) {
            ImageRuleSimpleCondition simpleCondition = (ImageRuleSimpleCondition) condition;
            Object propertyObject;
            try {
                propertyObject = JsonPath.read(properties, simpleCondition.getProperty());
            } catch (Exception e) {
                return String.format("cannot extract value for path '%s', failed with '%s'", simpleCondition.getProperty(), e.getMessage());
            }
            try {
                return evaluateSimpleCondition(simpleCondition, propertyObject);
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        if (condition instanceof ImageRuleCompositeCondition) {
            ImageRuleCompositeCondition compositeCondition = (ImageRuleCompositeCondition) condition;
            return evaluateCompositeCondition(compositeCondition, properties);
        }

        return String.format("rule condition '%s' not yet implemented", condition.getClass().getName());
    }

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

    public static String evaluateCompositeNotCondition(ImageRuleCompositeNotCondition condition, Object properties) {
        // evaluate all sub conditions
        String result = evaluateCondition(condition.getNot(), properties);

        if (Objects.isNull(result)) {
            return String.format("'not' condition expected to fail");
        }

        return null;
    }

    public static String evaluateSimpleCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        switch (condition.getOperator()) {
            case ImageRuleSimpleConditionOperatorType.equal:
                return evaluateEqualCondition(condition, extractedValue);
            case ImageRuleSimpleConditionOperatorType.lowerThan:
            case ImageRuleSimpleConditionOperatorType.lowerThanOrEqual:
            case ImageRuleSimpleConditionOperatorType.greaterThan:
            case ImageRuleSimpleConditionOperatorType.greaterThanOrEqual:
                return evaluateNumberCondition(condition, extractedValue);
            case ImageRuleSimpleConditionOperatorType.isInteger:
                return evaluateIsIntegerCondition(condition, extractedValue);
            case ImageRuleSimpleConditionOperatorType.occurrences:
                return evaluateOccurrencesCondition(condition, extractedValue);
            case ImageRuleSimpleConditionOperatorType.oneOf:
                return evaluateOneOfCondition(condition, extractedValue);
            default:
                return String.format("failed validation with unknown operator '%s'", condition.getOperator());
        }
    }

    public static String evaluateOneOfCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (Objects.isNull(condition.getValues())) {
            return String.format("oneOf operator requires at least one comparison value");
        }
        if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() == 1) {
                if (condition.getValues().contains(extractedList.get(0).toString())) {
                    // unpack success
                    return null;
                }
            }
        }
        if (!condition.getValues().contains(extractedValue.toString())) {
            return String.format("extracted value '%s' not one of '%s'", prettifyExtractedValue(extractedValue), condition.getValues());
        }
        return null;
    }

    public static String evaluateIsIntegerCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (Objects.nonNull(condition.getValues()) && condition.getValues().size() > 0) {
            logger.warn("ignoring values of 'isInteger' condition");
        }
        try {
            extractedValueToInteger(extractedValue);
        } catch (NumberFormatException e) {
            return String.format("could not parse extracted value '%s' as an integer", prettifyExtractedValue(extractedValue));
        }
        return null;
    }

    public static String evaluateOccurrencesCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (!(extractedValue instanceof List)) {
            return String.format(
                "cannot determine number of occurrences of extracted value '%s'",
                condition.getValues(),
                prettifyExtractedValue(extractedValue)
            );
        }
        Integer extractedLength = ((List<?>) extractedValue).size();
        Integer comparisonLength = condition.getValues().size();
        if (extractedLength != comparisonLength) {
            return String.format("extracted occurrences %d does not match expected occurrences %d", extractedLength, comparisonLength);
        }
        return null;
    }

    public static String evaluateEqualCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (Objects.isNull(condition.getValues()) || condition.getValues().size() == 0) {
            return String.format("equal operator requires comparison values");
        }
        if (condition.getValues().size() > 1) {
            return String.format("too many comparison values for equal operator (use oneOf instead?)");
        }

        String comparisonValue = condition.getValues().get(0);

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
                prettifyExtractedValue(extractedValue),
                condition.getValues().get(0)
            );
        }
        return null;
    }

    public static String evaluateNumberCondition(ImageRuleSimpleCondition condition, Object extractedValue) {
        if (condition.getValues().size() == 0) {
            return String.format("comparison value is required for condition operator '%s'", condition.getOperator());
        }
        if (condition.getValues().size() > 1) {
            return String.format("too many comparison values for condition operator '%s'", condition.getOperator());
        }
        Float comparisonFloat = Float.valueOf(condition.getValues().get(0));
        Float extractedFloat;
        try {
            extractedFloat = extractedValueToFloat(extractedValue);
        } catch (NumberFormatException e) {
            return String.format("could not parse extracted value '%s' as a number", prettifyExtractedValue(extractedValue));
        }

        switch (condition.getOperator()) {
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
                return String.format("failed validation with unknown number operator '{}'", condition.getOperator());
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
                    String.format("cannot convert multiple extracted values '%s' to number", prettifyExtractedValue(extractedValue))
                );
            }
            if (extractedList.get(0) instanceof String) {
                return Float.valueOf((String) extractedList.get(0));
            }
        }
        throw new NumberFormatException(
            String.format("cannot convert extracted value '%s' to number", prettifyExtractedValue(extractedValue))
        );
    }

    private static Integer extractedValueToInteger(Object extractedValue) throws NumberFormatException {
        if (extractedValue instanceof String) {
            return Integer.valueOf((String) extractedValue);
        } else if (extractedValue instanceof List) {
            List<?> extractedList = (List<?>) extractedValue;
            if (extractedList.size() != 1) {
                throw new NumberFormatException(
                    String.format("cannot convert multiple extracted values '%s' to number", prettifyExtractedValue(extractedValue))
                );
            }
            if (extractedList.get(0) instanceof String) {
                return Integer.valueOf((String) extractedList.get(0));
            }
        }
        throw new NumberFormatException(
            String.format("cannot convert extracted value '%s' to number", prettifyExtractedValue(extractedValue))
        );
    }

    private static String prettifyExtractedValue(Object properties) {
        try {
            if (properties instanceof String) {
                return (String) properties;
            }
            if (properties instanceof List) {
                List<?> propertyList = (List<?>) properties;
                if (propertyList.size() == 0) {
                    return "";
                }

                if (propertyList.size() == 1 && propertyList.get(0) instanceof String) {
                    return (String) propertyList.get(0);
                }

                return new ObjectMapper().writeValueAsString(properties);
            }
            return new ObjectMapper().writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            return properties.toString();
        }
    }

    private static List<Object> resolveContext(String context, Object properties) {
        logger.trace("resolve context jsonpath '{}'", context);
        Object matches = JsonPath.read(properties, context);

        // make sure to return a list, even if only one element matched
        @SuppressWarnings("unchecked")
        List<Object> contextObjects = (matches instanceof List)
            ? (List<Object>) matches
            : Collections.singletonList(matches).stream().filter(Objects::nonNull).toList();

        logger.trace("found {} matching context objects", contextObjects.size());
        return contextObjects;
    }
}
