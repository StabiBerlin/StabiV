package de.spkberlin.imagevalidation.common.rules;

import com.jayway.jsonpath.JsonPath;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(RuleEvaluator.class);

    private static final String DEFAULT_CONTEXT = "$.";

    public static List<String> evaluateRules(List<Rule> rules, Object properties) throws IOException {
        logger.trace("evaluate rules for the following properties:\n\n{}\n", PropertiesSerializer.serializeProperties(properties));
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Rule rule : rules) {
            logger.trace("evalute rule '{}'", Objects.nonNull(rule.getError()) ? rule.getError() : rule.getWarning());
            String context = Objects.nonNull(rule.getContext()) ? rule.getContext() : DEFAULT_CONTEXT;
            List<Object> contextObjects = resolveContext(context, properties);
            for (Object contextObject : contextObjects) {
                String conditionError = evaluateCondition(rule.getCondition(), contextObject);
                if (Objects.nonNull(conditionError)) {
                    if (Objects.nonNull(rule.getError())) {
                        logger.error("validation error: {} ({})", rule.getError(), conditionError);
                        errors.add(String.format("%s (%s)", rule.getError(), conditionError));
                    } else if (Objects.nonNull(rule.getWarning())) {
                        logger.warn("validation warning: {} ({})", rule.getWarning(), conditionError);
                        warnings.add(String.format("%s (%s)", rule.getWarning(), conditionError));
                    }
                }
            }
        }

        return errors;
    }

    public static String evaluateCondition(RuleCondition condition, Object properties) {
        if (condition instanceof RuleSimpleCondition) {
            RuleSimpleCondition simpleCondition = (RuleSimpleCondition) condition;
            if (simpleCondition.getOperator().equals("equal")) {
                Object propertyObject = JsonPath.read(properties, simpleCondition.getProperty());
                /*if (propertyObjects.size() != 1) {
                    return String.format("property path resolved to %s property, but expected exactly 1", propertyObjects.size());
                }*/
                if (!propertyObject.toString().equals(simpleCondition.getValue().toString())) {
                    return String.format("'%s' doesn't match rule comparison value '%s'", simpleCondition.getValue(), propertyObject);
                }
                // success
                return null;
            }

            return String.format("rule operator '%s' not implemented yet", simpleCondition.getOperator());
        }

        return String.format("rule condition '%s' not yet implemented", condition.getClass().getName());
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
