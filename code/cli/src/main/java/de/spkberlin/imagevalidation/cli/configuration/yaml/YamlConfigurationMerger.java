package de.spkberlin.imagevalidation.cli.configuration.yaml;

import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.common.rules.files.state.FileRule;
import de.spkberlin.imagevalidation.common.rules.files.state.FileRuleSeverityType;
import de.spkberlin.imagevalidation.common.rules.files.state.FileRuleSimpleCondition;
import de.spkberlin.imagevalidation.common.rules.files.state.FileRuleSimpleConditionOperatorType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
        // make sure input section exists
        if (Objects.isNull(config.getInput())) {
            config.setInput(new YamlInputSection());
        }
        // merge base directory
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
        // merge includes
        if (Objects.isNull(config.getInput().getIncludes())) {
            config.getInput().setIncludes(new ArrayList<>());
        }
        if (Objects.nonNull(includes)) {
            config.getInput().getIncludes().addAll(includes);
        }
        // merge excludes
        if (Objects.isNull(config.getInput().getExcludes())) {
            config.getInput().setExcludes(new ArrayList<>());
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
        // make sure extraction section exists
        if (Objects.isNull(config.getExtraction())) {
            config.setExtraction(new YamlExtractionSection());
        }
        // merge modules
        if (Objects.nonNull(modules) && !modules.isEmpty()) {
            config.getExtraction().setModules(modules);
        }
    }

    public static void mergeValidation(YamlConfiguration config, List<String> imageRules) {
        // make sure validation section exists
        if (Objects.isNull(config.getValidation())) {
            config.setValidation(new YamlValidationSection());
        }
        if (Objects.isNull(config.getValidation().getImageRules())) {
            config.getValidation().setImageRules(new ArrayList<>());
        }
        // add rules provided via command line options
        List<FileRule> additionalImageRules = imageRules
            .stream()
            .map(r -> getImageRuleFromCommandLineOption(r))
            .toList();
        config.getValidation().getImageRules().addAll(additionalImageRules);
    }

    public static FileRule getImageRuleFromCommandLineOption(String imageRuleText) {
        String[] ruleTextSplit = imageRuleText.split("\\|", 3);
        if (ruleTextSplit.length != 3) {
            throw new RuntimeException(String.format("image rule '%s' needs to consist of three parts separated by `|`", imageRuleText));
        }

        FileRuleSimpleCondition condition = new FileRuleSimpleCondition();
        condition.setProperty(ruleTextSplit[0]);
        condition.setOperator(FileRuleSimpleConditionOperatorType.valueOf(ruleTextSplit[1]));
        condition.setValues(Arrays.asList(ruleTextSplit[2].split(",")));

        FileRule rule = new FileRule();
        rule.setContext("$");
        rule.setMessage(String.format("command line rule '%s' must be fulfilled", imageRuleText));
        rule.setSeverity(FileRuleSeverityType.error);
        rule.setCondition(condition);

        return rule;
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
        // make sure extraction section exists
        if (Objects.isNull(config.getOutput())) {
            config.setOutput(new YamlOutputSection());
        }
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
