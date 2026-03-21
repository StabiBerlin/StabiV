package de.spkberlin.imagevalidation.cli.configuration;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationCsvSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationExtractionSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationInputSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationLoggingSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationLoggingVerbosityType;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationOutputSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationValidationSection;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.cli.output.csv.CsvPropertyColumn;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSeverityType;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSimpleCondition;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRuleSimpleConditionOperatorType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigurationMerger {

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
        ConfigurationRoot config,
        Path cwd,
        Path configPath,
        Path baseDirectory,
        List<String> includes,
        List<String> excludes
    ) {
        // make sure input section exists
        if (Objects.isNull(config.getInput())) {
            config.setInput(new ConfigurationInputSection());
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
    public static void mergeExtraction(ConfigurationRoot config, List<ExtractorType> modules) {
        // make sure extraction section exists
        if (Objects.isNull(config.getExtraction())) {
            config.setExtraction(new ConfigurationExtractionSection());
        }
        // merge modules
        if (Objects.nonNull(modules) && !modules.isEmpty()) {
            config.getExtraction().setModules(modules);
        }
    }

    public static void mergeValidation(ConfigurationRoot config, List<String> imageRules) {
        // make sure validation section exists
        if (Objects.isNull(config.getValidation())) {
            config.setValidation(new ConfigurationValidationSection());
        }
        if (Objects.isNull(config.getValidation().getImageRules())) {
            config.getValidation().setImageRules(new ArrayList<>());
        }
        // add rules provided via command line options
        List<ImageRule> additionalImageRules = imageRules
            .stream()
            .map(r -> getImageRuleFromCommandLineOption(r))
            .toList();
        config.getValidation().getImageRules().addAll(additionalImageRules);
    }

    public static ImageRule getImageRuleFromCommandLineOption(String imageRuleText) {
        String[] ruleTextSplit = imageRuleText.split("\\|", 3);
        if (ruleTextSplit.length != 3) {
            throw new RuntimeException(String.format("image rule '%s' needs to consist of three parts separated by `|`", imageRuleText));
        }

        ImageRuleSimpleCondition condition = new ImageRuleSimpleCondition();
        condition.setProperty(ruleTextSplit[0]);
        condition.setOperator(ImageRuleSimpleConditionOperatorType.valueOf(ruleTextSplit[1]));
        condition.setValues(Arrays.asList(ruleTextSplit[2].split(",")));

        ImageRule rule = new ImageRule();
        rule.setContext("$");
        rule.setMessage(String.format("command line rule '%s' must be fulfilled", imageRuleText));
        rule.setSeverity(ImageRuleSeverityType.error);
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
    public static void mergeOutput(
        ConfigurationRoot config,
        String csvPath,
        List<String> csvColumns,
        Boolean json,
        ConfigurationLoggingVerbosityType verbosity
    ) {
        // make sure extraction section exists
        if (Objects.isNull(config.getOutput())) {
            config.setOutput(new ConfigurationOutputSection());
        }
        // make sure csv section exists
        if (Objects.isNull(config.getOutput().getCsv())) {
            config.getOutput().setCsv(new ConfigurationCsvSection());
        }
        // overwrite csv path
        if (Objects.nonNull(csvPath) && !csvPath.isEmpty()) {
            config.getOutput().getCsv().setPath(csvPath);
        }
        // add csv columns
        if (Objects.isNull(config.getOutput().getCsv().getColumns())) {
            config.getOutput().getCsv().setColumns(new ArrayList<>());
        }
        List<CsvPropertyColumn> additionalColumns = csvColumns
            .stream()
            .map(c -> getCsvPropertyColumnFromCommandLineOption(c))
            .toList();
        config.getOutput().getCsv().getColumns().addAll(additionalColumns);
        // overwrite json output
        if (Objects.isNull(config.getOutput().getLogging())) {
            config.getOutput().setLogging(new ConfigurationLoggingSection());
        }
        if (Objects.nonNull(json)) {
            config.getOutput().getLogging().setJson(json);
        }
        // overwrite verbosity
        if (Objects.nonNull(verbosity)) {
            config.getOutput().getLogging().setVerbosity(verbosity);
        }
    }

    public static CsvPropertyColumn getCsvPropertyColumnFromCommandLineOption(String columnText) {
        String[] columnTextSplit = columnText.split("\\|", 2);
        if (columnTextSplit.length != 2) {
            throw new RuntimeException(String.format("csv column '%s' needs to consist of two parts separated by `|`", columnText));
        }

        return new CsvPropertyColumn(columnTextSplit[0], columnTextSplit[1]);
    }
}
