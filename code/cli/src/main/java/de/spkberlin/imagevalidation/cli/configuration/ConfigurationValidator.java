package de.spkberlin.imagevalidation.cli.configuration;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    /**
     * Return true if configuration is valid. Logs any errors.
     *
     * @param config the configuration to valdiate
     * @return true if configuration is valid
     */
    public static boolean validateConfiguration(ConfigurationRoot config) {
        logger.debug("validate configuration");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<ConfigurationRoot>> violations = validator.validate(config);

        if (violations.size() > 0) {
            logger.error("found {} problems validating configuration", violations.size());
            for (ConstraintViolation<ConfigurationRoot> violation : violations) {
                logger.error("error in configuration: {} {}", violation.getPropertyPath(), violation.getMessage());
            }
            return false;
        }

        logger.debug("configuration is valid");
        return true;
    }
}
