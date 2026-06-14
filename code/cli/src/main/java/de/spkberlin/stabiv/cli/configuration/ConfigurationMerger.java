package de.spkberlin.stabiv.cli.configuration;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationCsvSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelLayoutAutoFit;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelLayoutFixed;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelLayoutSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExcelStyleSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationExtractionSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationInputSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationLoggingSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationLoggingVerbosityType;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationOutputChecksumSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationOutputSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationPath;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationProcessingSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationReportSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationValidationDirectoryRules;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationValidationSection;
import de.spkberlin.stabiv.cli.extraction.ExtractorType;
import de.spkberlin.stabiv.cli.path.GlobResolver;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsecutiveNumberingCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsistencyCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsAggregateType;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryPropertyDeviationsRule;
import de.spkberlin.stabiv.common.validation.images.state.ImageRule;
import de.spkberlin.stabiv.common.validation.images.state.ImageRuleSimpleCondition;
import de.spkberlin.stabiv.common.validation.images.state.ImageRuleSimpleConditionOperatorType;
import de.spkberlin.stabiv.common.validation.state.ValidationFailureSeverity;
import de.spkberlin.stabiv.common.validation.state.ValidationReportColumn;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConfigurationMerger {

    /**
     * Merge input section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param cwd the current working directory
     * @param cliInputArguments the nameless input arguments specified via CLI
     * @param includes the list of includes filepaths specified with CLI option
     * @param excludes the list of exclided filepaths specified with CLI option
     */
    public static void mergeInput(
        ConfigurationRoot config,
        Path cwd,
        List<String> cliInputArguments,
        List<String> includes,
        List<String> excludes
    ) {
        // make sure input section exists
        if (Objects.isNull(config.getInput())) {
            config.setInput(new ConfigurationInputSection());
        }

        // make sure base directory is defined
        if (cliInputArguments.isEmpty() && Objects.isNull(config.getInput().getBaseDirectory())) {
            // use cwd as base directory
            config.getInput().setBaseDirectory(cwd);
        }

        if (namelessInputArgumentIsSingleDirectory(cliInputArguments)) {
            // use input argument as base directory
            config.getInput().setBaseDirectory(Path.of(cliInputArguments.get(0)));
        }

        // make sure includes are defined
        if (Objects.isNull(config.getInput().getIncludes())) {
            config.getInput().setIncludes(new ArrayList<>());
        }
        mergeConfigurationPaths(config.getInput().getIncludes(), config.getInput().getBaseDirectory(), includes);

        // add default include pattern "**" if none are specified
        if (config.getInput().getIncludes().isEmpty()) {
            config.getInput().getIncludes().add(new ConfigurationPath("**", config.getInput().getBaseDirectory()));
        }

        // merge excludes
        if (Objects.isNull(config.getInput().getExcludes())) {
            config.getInput().setExcludes(new ArrayList<>());
        }
        mergeConfigurationPaths(config.getInput().getExcludes(), config.getInput().getBaseDirectory(), excludes);

        mergeInputNamelessArguments(config, cwd, cliInputArguments);
    }

    /**
     * Returns true if there is only one nameless input argument referencing a directory.
     *
     * @param cliInputArguments the nameless input arguments specified via CLI
     * @return true if there is only one nameless input argument referencing a directory
     */
    private static boolean namelessInputArgumentIsSingleDirectory(List<String> cliInputArguments) {
        boolean isSingleDirectory =
            cliInputArguments.size() == 1 &&
            !GlobResolver.isGlobPattern(cliInputArguments.get(0)) &&
            Files.isDirectory(Path.of(cliInputArguments.get(0)));
        return isSingleDirectory;
    }

    /**
     * Merge nameless input arguments that are not a single directory, meaning, include and exclude rules are overwritten.
     *
     * @param config the yaml configuration
     * @param cwd the current working directory
     * @param cliInputArguments the nameless input arguments specified via CLI
     * @param namelessArgumentIsSingleDirectory whether the nameless input arguments matches a single directory
     */
    private static void mergeInputNamelessArguments(ConfigurationRoot config, Path cwd, List<String> cliInputArguments) {
        if (!cliInputArguments.isEmpty() && !namelessInputArgumentIsSingleDirectory(cliInputArguments)) {
            // interprete input arguments as include rules and ignore all previously defined include and exclude rules
            config.getInput().setBaseDirectory(cwd);
            config
                .getInput()
                .setIncludes(
                    cliInputArguments
                        .stream()
                        .map(arg -> new ConfigurationPath(arg, cwd))
                        .toList()
                );
            config.getInput().setExcludes(Collections.emptyList());
        }
    }

    /**
     * Merge processing section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param parallelThreads the number of executions threads for image validation provided via CLI option
     * @param checksumAlgorithm the checksum algorithm that is provided via CLI option
     */
    public static void mergeProcessing(ConfigurationRoot config, Integer parallelThreads, String checksumAlgorithm) {
        // make sure processing section exists
        if (Objects.isNull(config.getProcessing())) {
            config.setProcessing(new ConfigurationProcessingSection());
        }
        // set parallel threads if provided
        if (Objects.nonNull(parallelThreads)) {
            config.getProcessing().setThreads(parallelThreads);
        }
        // set checksum algorithm if provided
        if (Objects.nonNull(checksumAlgorithm)) {
            config.getProcessing().setChecksumAlgorithm(checksumAlgorithm);
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

    /**
     * Merge the "validation" section of the yaml configuration with relevant CLI options.
     *
     * @param config the yaml configuration
     * @param imageRules the list of additional image validation rules provided via CLI options
     * @param consistentProperties the list of additional properties to check to be consistent via CLI option
     * @param consecutiveNumberingFilenameRegExp the filename regexp for checking consecutive numbering provided via CLI option
     * @param propertyDeviations the list of additional property deviation rules provided via CLI options
     */
    public static void mergeValidation(
        ConfigurationRoot config,
        List<String> imageRules,
        List<String> consistentProperties,
        String consecutiveNumberingFilenameRegExp,
        List<String> propertyDeviations
    ) {
        // make sure validation section exists
        if (Objects.isNull(config.getValidation())) {
            config.setValidation(new ConfigurationValidationSection());
        }
        mergeValidationDirectoryRules(config, consistentProperties, consecutiveNumberingFilenameRegExp, propertyDeviations);
        mergeValidationImageRules(config, imageRules);
    }

    private static void mergeValidationDirectoryRules(
        ConfigurationRoot config,
        List<String> consistentProperties,
        String consecutiveNumberingFilenameRegExp,
        List<String> propertyDeviations
    ) {
        // make sure directory section exists
        if (Objects.isNull(config.getValidation().getDirectoryRules())) {
            config.getValidation().setDirectoryRules(new ConfigurationValidationDirectoryRules());
        }
        // make sure consistent properties exists
        if (Objects.isNull(config.getValidation().getDirectoryRules().getConsistentProperties())) {
            config.getValidation().getDirectoryRules().setConsistentProperties(new ArrayList<>());
        }
        // add additional consistent properties
        if (Objects.nonNull(consistentProperties)) {
            config
                .getValidation()
                .getDirectoryRules()
                .getConsistentProperties()
                .addAll(
                    consistentProperties
                        .stream()
                        .map(p -> getConsistencyConditionFromCommandLineOption(p))
                        .toList()
                );
        }
        // make sure consecutive numbering exists
        if (Objects.isNull(config.getValidation().getDirectoryRules().getConsecutiveNumbering())) {
            config.getValidation().getDirectoryRules().setConsecutiveNumbering(new DirectoryConsecutiveNumberingCondition());
        }
        // set consecutive number check if CLI options is provided
        if (Objects.nonNull(consecutiveNumberingFilenameRegExp)) {
            config
                .getValidation()
                .getDirectoryRules()
                .setConsecutiveNumbering(new DirectoryConsecutiveNumberingCondition(consecutiveNumberingFilenameRegExp));
        }
        // make sure property deviations list exists
        if (Objects.isNull(config.getValidation().getDirectoryRules().getPropertyDeviations())) {
            config.getValidation().getDirectoryRules().setPropertyDeviations(new ArrayList<>());
        }
        if (Objects.nonNull(propertyDeviations)) {
            List<DirectoryPropertyDeviationsRule> additionalRules = propertyDeviations
                .stream()
                .map(r -> getPropertyDeviationRuleFromCommandLineOption(r))
                .toList();
            config.getValidation().getDirectoryRules().getPropertyDeviations().addAll(additionalRules);
        }
    }

    private static DirectoryPropertyDeviationsRule getPropertyDeviationRuleFromCommandLineOption(String propertyDeviationText) {
        String[] ruleTextSplit = propertyDeviationText.split("/", 3);
        if (ruleTextSplit.length != 3) {
            throw new RuntimeException(
                String.format("property deviation rule '%s' needs to consist of three parts separated by /", propertyDeviationText)
            );
        }

        DirectoryPropertyDeviationsRule rule = new DirectoryPropertyDeviationsRule(
            ruleTextSplit[0],
            ruleTextSplit[1],
            true,
            new DirectoryPropertyDeviationsCondition(
                DirectoryPropertyDeviationsAggregateType.average,
                Double.parseDouble(ruleTextSplit[2]),
                Boolean.FALSE
            ),
            Collections.emptyList()
        );

        return rule;
    }

    private static DirectoryConsistencyCondition getConsistencyConditionFromCommandLineOption(String consistentProperty) {
        return new DirectoryConsistencyCondition(String.format("command line property '%s'", consistentProperty), consistentProperty);
    }

    private static void mergeValidationImageRules(ConfigurationRoot config, List<String> imageRules) {
        // make sure image rule list exists
        if (Objects.isNull(config.getValidation().getImageRules())) {
            config.getValidation().setImageRules(new ArrayList<>());
        }
        // add rules provided via command line options
        if (Objects.nonNull(imageRules)) {
            List<ImageRule> additionalImageRules = imageRules
                .stream()
                .map(r -> getImageRuleFromCommandLineOption(r))
                .toList();
            config.getValidation().getImageRules().addAll(additionalImageRules);
        }
    }

    private static ImageRule getImageRuleFromCommandLineOption(String imageRuleText) {
        String[] ruleTextSplit = imageRuleText.split("/", 3);
        if (ruleTextSplit.length != 3) {
            throw new RuntimeException(String.format("image rule '%s' needs to consist of three parts separated by /", imageRuleText));
        }

        ImageRuleSimpleCondition condition = new ImageRuleSimpleCondition();
        condition.setProperty(ruleTextSplit[0]);
        condition.setOperator(ImageRuleSimpleConditionOperatorType.valueOf(ruleTextSplit[1]));
        condition.setValues(Arrays.asList(ruleTextSplit[2].split(",")));

        ImageRule rule = new ImageRule();
        rule.setContext("$");
        rule.setMessage(String.format("command line rule '%s' must be fulfilled", imageRuleText));
        rule.setSeverity(ValidationFailureSeverity.error);
        rule.setCondition(condition);

        return rule;
    }

    /**
     * Merge output section of yaml configuration with CLI options.
     *
     * @param config the yaml configuration
     * @param cwd the current working directory
     * @param cliInputArguments the nameless input arguments specified via CLI
     * @param cliOutputBaseDirectory the current working directory
     * @param csvPath the csv output path specified with CLI option
     * @param excelPath the excel output path specified with CLI option
     * @param reportColumns add list of additional property columns as provided by CLI option
     * @param checksumFilename the filename template for the checksum files provided via CLI option
     * @param json whether json output should be generated specified with CLI option
     * @param verbosity logging verbositry specified with CLI option
     */
    public static void mergeOutput(
        ConfigurationRoot config,
        Path cwd,
        List<String> cliInputArguments,
        Path cliOutputBaseDirectory,
        String csvPath,
        String excelPath,
        List<String> reportColumns,
        String checksumFilename,
        Boolean json,
        ConfigurationLoggingVerbosityType verbosity
    ) {
        // make sure extraction section exists
        if (Objects.isNull(config.getOutput())) {
            config.setOutput(new ConfigurationOutputSection());
        }

        // set output base directory based on cli options
        if (Objects.isNull(config.getOutput().getBaseDirectory()) && namelessInputArgumentIsSingleDirectory(cliInputArguments)) {
            config.getOutput().setBaseDirectory(Path.of(cliInputArguments.get(0)));
        } else if (Objects.isNull(cliOutputBaseDirectory) && Objects.isNull(config.getOutput().getBaseDirectory())) {
            config.getOutput().setBaseDirectory(cwd);
        } else if (Objects.nonNull(cliOutputBaseDirectory)) {
            config.getOutput().setBaseDirectory(cliOutputBaseDirectory);
        }

        mergeOutputReportSection(config.getOutput(), csvPath, excelPath, reportColumns);
        mergeOutputChecksumSection(config.getOutput(), checksumFilename);

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

    private static void mergeOutputReportSection(
        ConfigurationOutputSection config,
        String cliCsvPath,
        String cliExcelPath,
        List<String> cliReportColumns
    ) {
        // make sure csv section exists
        if (Objects.isNull(config.getReport())) {
            config.setReport(new ConfigurationReportSection());
        }

        mergeOutputReportCsvSection(config, cliCsvPath);
        mergeOutputReportExcelSection(config, cliExcelPath);

        // add csv columns
        if (Objects.isNull(config.getReport().getColumns())) {
            config.getReport().setColumns(new ArrayList<>());
        }
        List<ValidationReportColumn> additionalColumns = cliReportColumns
            .stream()
            .map(c -> getReportPropertyColumnFromCommandLineOption(c))
            .toList();
        config.getReport().getColumns().addAll(additionalColumns);
    }

    private static void mergeOutputReportCsvSection(ConfigurationOutputSection config, String cliCsvPath) {
        // make sure csv section exists
        if (Objects.isNull(config.getReport().getCsv())) {
            config.getReport().setCsv(new ConfigurationCsvSection());
        }

        // overwrite csv path from cli
        if (Objects.nonNull(cliCsvPath)) {
            config.getReport().getCsv().setPath(new ConfigurationPath(cliCsvPath, config.getBaseDirectory()));
        } else if (Objects.nonNull(config.getReport().getCsv().getPath())) {
            mergeConfigurationPath(config.getReport().getCsv().getPath(), config.getBaseDirectory());
        }
    }

    private static void mergeOutputReportExcelSection(ConfigurationOutputSection config, String cliExcelPath) {
        // make sure excel section exists
        if (Objects.isNull(config.getReport().getExcel())) {
            config.getReport().setExcel(new ConfigurationExcelSection());
        }

        // overwrite excel path from cli
        if (Objects.nonNull(cliExcelPath)) {
            config.getReport().getExcel().setPath(new ConfigurationPath(cliExcelPath, config.getBaseDirectory()));
        } else if (Objects.nonNull(config.getReport().getExcel().getPath())) {
            mergeConfigurationPath(config.getReport().getExcel().getPath(), config.getBaseDirectory());
        }

        // make sure layout section exists
        if (Objects.isNull(config.getReport().getExcel().getLayout())) {
            config.getReport().getExcel().setLayout(new ConfigurationExcelLayoutSection());
        }

        // make sure auto-fit options exists
        if (Objects.isNull(config.getReport().getExcel().getLayout().getAutoFitColumns())) {
            config.getReport().getExcel().getLayout().setAutoFitColumns(new ConfigurationExcelLayoutAutoFit());
        }

        // make sure fixed options exists
        if (Objects.isNull(config.getReport().getExcel().getLayout().getFixed())) {
            config.getReport().getExcel().getLayout().setFixed(new ConfigurationExcelLayoutFixed());
        }

        // make sure style section exists
        if (Objects.isNull(config.getReport().getExcel().getStyle())) {
            config.getReport().getExcel().setStyle(new ConfigurationExcelStyleSection());
        }
    }

    private static void mergeOutputChecksumSection(ConfigurationOutputSection config, String checksumFilename) {
        // make sure checksum section exists
        if (Objects.isNull(config.getChecksum())) {
            config.setChecksum(new ConfigurationOutputChecksumSection());
        }
        if (Objects.nonNull(checksumFilename)) {
            config.getChecksum().setFilename(checksumFilename);
        }
    }

    private static ValidationReportColumn getReportPropertyColumnFromCommandLineOption(String cliColumn) {
        String[] columnTextSplit = cliColumn.split("/", 2);
        if (columnTextSplit.length != 2) {
            throw new RuntimeException(String.format("csv column '%s' needs to consist of two parts separated by /", cliColumn));
        }

        return new ValidationReportColumn(columnTextSplit[0], columnTextSplit[1]);
    }

    private static void mergeConfigurationPaths(List<ConfigurationPath> configPathList, Path baseDirectory, List<String> cliPaths) {
        // make sure config include paths have base directory of config file
        for (ConfigurationPath path : configPathList) {
            mergeConfigurationPath(path, baseDirectory);
        }
        // add cli paths
        if (Objects.nonNull(cliPaths)) {
            List<ConfigurationPath> additional = cliPaths
                .stream()
                .map(p -> new ConfigurationPath(p, baseDirectory))
                .toList();
            configPathList.addAll(additional);
        }
    }

    private static void mergeConfigurationPath(ConfigurationPath configPath, Path baseDirectory) {
        if (Objects.isNull(configPath.getBaseDirectory())) {
            configPath.setBaseDirectory(baseDirectory);
        }
    }
}
