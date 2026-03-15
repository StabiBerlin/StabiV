package de.spkberlin.imagevalidation.cli.common;

import java.net.URI;
import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;

public class Logging {

    public static Configuration getLoggingConfiguration(boolean json) {
        ConfigurationFactory configFactory = ConfigurationFactory.getInstance();
        AbstractConfiguration commonConfig = (AbstractConfiguration) configFactory.getConfiguration(
            null,
            null,
            URI.create("classpath:log4j2.common.xml")
        );
        AbstractConfiguration consoleConfig = (AbstractConfiguration) configFactory.getConfiguration(
            null,
            null,
            json ? URI.create("classpath:log4j2.json.xml") : URI.create("classpath:log4j2.text.xml")
        );
        return new CompositeConfiguration(Arrays.asList(commonConfig, consoleConfig));
    }

    public static void setLogMode(boolean json) {
        Configurator.reconfigure(Logging.getLoggingConfiguration(json));
    }

    public static void setLogLevel(boolean verbose) {
        Configurator.setLevel("de.spkberlin", verbose ? Level.DEBUG : Level.INFO);
    }
}
