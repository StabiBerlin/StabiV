package de.spkberlin.imagevalidation.cli.common;

import java.net.URI;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {

    private static final Logger logger = LoggerFactory.getLogger(Logging.class);

    private static final Level[] VERBOSITY_LEVELS = { Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE };

    /**
     * Return logging configuration.
     *
     * @param json whether json-based configuration should be loaded
     * @return the logging configuration
     */
    public static Configuration getLoggingConfiguration(boolean json) {
        ConfigurationFactory configFactory = ConfigurationFactory.getInstance();
        AbstractConfiguration commonConfig = (AbstractConfiguration) configFactory.getConfiguration(
            null,
            null,
            URI.create("classpath:log4j2.xml")
        );
        AbstractConfiguration consoleConfig = (AbstractConfiguration) configFactory.getConfiguration(
            null,
            null,
            json ? URI.create("classpath:log4j2.json.xml") : URI.create("classpath:log4j2.text.xml")
        );
        return new CompositeConfiguration(Arrays.asList(commonConfig, consoleConfig));
    }

    /**
     * Set logging mode depending on whether json-output should be generated.
     *
     * @param json whether json-based configuration should be loaded
     */
    public static void setLogMode(boolean json) {
        logger.debug("set {} log mode", json ? "json" : "text");
        Configurator.reconfigure(Logging.getLoggingConfiguration(json));
    }

    /**
     * Set the main logging level.
     *
     * @param verbosity verbosity as integer (error=0, warning=1, ...)
     */
    public static void setLogLevel(int verbosity) {
        int idx = Math.max(0, Math.min(VERBOSITY_LEVELS.length - 1, verbosity));
        Configurator.setLevel("de.spkberlin", VERBOSITY_LEVELS[idx]);
    }
}
