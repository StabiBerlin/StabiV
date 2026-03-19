package de.spkberlin.imagevalidation.cli.configuration.yaml;

import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class YamlConfigurationMerger {

    /**
     * Merge input section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param cwd the current working directory
     * @param configPath the path to the configuration file
     * @param baseDirectory the base directory specified with CLI option
     * @param includes the list of includes filepaths specified with CLI option
     * @param excludes the list of exclided filepaths specified with CLI option
     */
    public static void mergeInput(
        YamlConfiguration config,
        Path cwd,
        Path configPath,
        Path baseDirectory,
        List<String> includes,
        List<String> excludes
    ) {
        if (Objects.nonNull(baseDirectory)) {
            // cli option overwrites everything
            config.getInput().setBaseDirectory(baseDirectory);
        } else if (Objects.isNull(config.getInput().getBaseDirectory())) {
            // default value for base directory
            if (Objects.nonNull(configPath)) {
                // use parent of config filepath
                config.getInput().setBaseDirectory(configPath.getParent());
            } else {
                // fallback to current working directory
                config.getInput().setBaseDirectory(cwd);
            }
        }
        if (Objects.nonNull(includes)) {
            config.getInput().getIncludes().addAll(includes);
        }
        if (Objects.nonNull(excludes)) {
            config.getInput().getExcludes().addAll(excludes);
        }
    }

    /**
     * Merge extraction section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param modules the list of extraction modules specified with CLI option
     */
    public static void mergeExtraction(YamlConfiguration config, List<ExtractorType> modules) {
        if (Objects.nonNull(modules) && !modules.isEmpty()) {
            config.getExtraction().setModules(modules);
        }
    }

    /**
     * Merge output section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param csvPath the csv output path specified with CLI option
     * @param json whether json output should be generated specified with CLI option
     * @param verbosity logging verbositry specified with CLI option
     */
    public static void mergeOutput(YamlConfiguration config, String csvPath, Boolean json, YamlLoggingVerbosityType verbosity) {
        if (Objects.nonNull(csvPath) && !csvPath.isEmpty()) {
            config.getOutput().getCsv().setPath(csvPath);
        }
        if (Objects.nonNull(json)) {
            config.getOutput().getLogging().setJson(json);
        }
        if (Objects.nonNull(verbosity)) {
            config.getOutput().getLogging().setVerbosity(verbosity);
        }
    }
}
