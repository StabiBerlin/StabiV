package de.spkberlin.imagevalidation.cli.commands;

import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlConfiguration;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlConfigurationMerger;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlConfigurationParser;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlConfigurationWriter;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlLoggingVerbosityType;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.cli.output.json.JsonLogWriter;
import de.spkberlin.imagevalidation.cli.path.FileUtils;
import de.spkberlin.imagevalidation.cli.path.GlobResolver;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.rules.files.FileRuleEvaluator;
import de.spkberlin.imagevalidation.common.rules.files.state.FileRule;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "validate",
    description = """
    %nValidate all image files according to a specification.

    @|bold Examples|@

    Validate all TIF files in all subdirectories and check JHove has detected them as wellformed:%n
      ${COMMAND-FULL-NAME} --rule="$.jhove.wellformed|equal|true" /path/to/images/**/*.tif

    Use a config file to define all validation options:%n
      ${COMMAND-FULL-NAME} --config /path/to/config.yaml
    """,
    showAtFileInUsageHelp = true, // allow to provide list of directories and files as a file instead of as cli arguments
    optionListHeading = "%n@|bold General Options|@%n%n",
    exitCodeListHeading = "%n@|bold Exit Codes|@%n%n",
    exitCodeList = {
        "0:successful validation without errors",
        "1:generic program error, validation aborted",
        "2:no program errors, but validation finished with errors",
        "64:an unknown option or invalid parameter was specified",
    }
)
public class ValidateCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ValidateCommand.class);

    private static final String CWD = System.getProperty("user.dir");

    private static List<YamlLoggingVerbosityType> VERBOSITY_LEVELS = List.of(
        YamlLoggingVerbosityType.error,
        YamlLoggingVerbosityType.warning,
        YamlLoggingVerbosityType.info,
        YamlLoggingVerbosityType.debug,
        YamlLoggingVerbosityType.trace
    );

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Configuration Options|@%n%n", order = 2, exclusive = false)
    public ConfigOptions configOptions = new ConfigOptions();

    static class ConfigOptions {

        /** The filepath to the configuration file. */
        @Option(names = { "--config" }, description = "Configuration file containing additional options")
        String configFilepath;

        /** The extraction modules that will be applied. */
        @Option(
            names = { "--extractors" },
            split = ",",
            defaultValue = "exiftool,jhove",
            paramLabel = "module",
            description = "Extraction modules that are applied to each image file"
        )
        ExtractorType[] extractors = {};

        @Option(
            names = { "--rule" },
            paramLabel = "rule",
            arity = "0..*",
            description = "Validation rules that are checked against extracted image properties"
        )
        String[] rules = {};
    }

    @ArgGroup(validate = true, heading = "%n@|bold Output Options|@%n%n", order = 3, exclusive = false)
    public OutputOptions outputOptions = new OutputOptions();

    static class OutputOptions {

        /** The filepath to the generated CSV file. */
        @Option(names = { "--csv" }, description = "CSV output file")
        String csvOutputFilepath;

        /** Wether to use json based log output. */
        @Option(names = { "--json" }, description = "print validation results in JSON format to stdout (and other log messages to stderr)")
        boolean jsonLogging;

        /** Wether to show additional log messages. */
        @Option(names = { "-v", "--verbose" }, description = "print more logging (use `-vv` for debug logging or `-vvv` for trace logging)")
        boolean[] verbosity = {};
    }

    @ArgGroup(validate = true, heading = "%n@|bold Input Options|@%n%n", order = 1, exclusive = false)
    public InputOptions inputOptions = new InputOptions();

    static class InputOptions {

        @Option(
            names = { "--baseDirectory" },
            paramLabel = "DIR",
            arity = "0..1",
            description = "the base directory that is used to resolve relative file paths%n  Default: current working directory"
        )
        String baseDirectory;

        @Option(
            names = { "--exclude" },
            paramLabel = "PATHS",
            arity = "0..*",
            description = """
            one ore multiple files, directories or glob patterns that are excluded from validation

            Examples
            - `**/ignore` - do not validate files or subdirectories called "ignore"
            - `**.jpg` - do not validate jpg files
            - `/path/to/directory` - do not validate files in this specific directory
            """
        )
        String[] excludes = {};

        /** The list of directories and files that are being validated. */
        @Parameters(
            paramLabel = "PATHS",
            arity = "0..*",
            description = """
            one or multiple files, directories or glob patterns that are scanned for files to validate

            Examples
            - `/path/to/directory` - validate all files from all subdirectories
            - `/path/to/directory/*.tif` - validate tif files in this directory not considering subdirectories
            - `/path/to/directory/**/*.tif` - validate tif files in all subdirectories
            - `/path/to/directory/**/*.{tif,jpg}` - validate tif and jpg files in all subdirectories
            - `../relative/path/to/directory` - relative path with respect to current working directory
            """
        )
        String[] includes = {};
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(outputOptions.jsonLogging);
        Logging.setLogLevel(outputOptions.verbosity.length + 1);

        logger.info("run validate command");
        return doValidation(loadConfiguration());
    }

    /**
     * Load the main Yaml configuration (or generate a default configuration).
     *
     * @return the yaml configuration
     * @throws IOException in case filepaths cannot be evaluated
     */
    public YamlConfiguration loadConfiguration() throws IOException {
        YamlConfiguration config = new YamlConfiguration();

        Path baseDirectory = Objects.nonNull(inputOptions.baseDirectory)
            ? Path.of(inputOptions.baseDirectory).normalize().toAbsolutePath()
            : null;
        Path cwd = Objects.nonNull(baseDirectory) ? baseDirectory : Path.of(CWD).normalize().toAbsolutePath();
        Path configPath = null;

        if (Objects.nonNull(configOptions.configFilepath)) {
            logger.info("load configuration file '{}", configOptions.configFilepath);
            configPath = Path.of(configOptions.configFilepath).normalize().toAbsolutePath();
            config = YamlConfigurationParser.parseConfigurationFile(configPath);
        }

        List<String> absoluteIncludes = Arrays.asList(inputOptions.includes)
            .stream()
            .map(p -> GlobResolver.makePatternAbsolute(p, cwd))
            .toList();

        List<String> absoluteExcludes = Arrays.asList(inputOptions.excludes)
            .stream()
            .map(p -> GlobResolver.makePatternAbsolute(p, cwd))
            .toList();

        logger.debug("merge configuration with command line options");
        YamlConfigurationMerger.mergeInput(config, cwd, configPath, baseDirectory, absoluteIncludes, absoluteExcludes);
        YamlConfigurationMerger.mergeExtraction(config, Arrays.asList(configOptions.extractors));
        YamlConfigurationMerger.mergeOutput(
            config,
            outputOptions.csvOutputFilepath,
            outputOptions.jsonLogging,
            VERBOSITY_LEVELS.get(outputOptions.verbosity.length + 1)
        );

        logger.trace(
            "yaml configuration after merging with command line options:\n\n{}",
            YamlConfigurationWriter.writeConfigurationFile(config)
        );

        return config;
    }

    /**
     * Find all files and directories that need to be validated.
     *
     * @param config the main yaml configuration
     * @return all directories and their files that need to be validated
     * @throws IOException in case filepaths cannot be evaluated
     */
    public static Map<Path, List<Path>> resolveFiles(YamlConfiguration config) throws IOException {
        logger.info("resolve files and directories");
        List<Path> resolvedFiles = GlobResolver.resolveMany(
            config.getInput().getIncludes(),
            config.getInput().getExcludes(),
            config.getInput().getBaseDirectory()
        );

        if (resolvedFiles.isEmpty()) {
            logger.warn("no files to validate");
        }

        Map<Path, List<Path>> filesByDirectory = FileUtils.mapFilesByDirectory(resolvedFiles);

        return filesByDirectory;
    }

    /**
     * Do actual validation of files.
     *
     * @param config the main yaml configuration
     * @return exit status
     * @throws IOException in case filepaths cannot be evaluated
     */
    public static Integer doValidation(YamlConfiguration config) throws IOException {
        Logging.setLogMode(config.getOutput().getLogging().getJson());
        Logging.setLogLevel(VERBOSITY_LEVELS.indexOf(config.getOutput().getLogging().getVerbosity()));

        Map<Path, List<Path>> filesByDirectory = resolveFiles(config);

        Map<ExtractorType, Extractor> extractors = Map.of(
            ExtractorType.exiftool,
            new ExiftoolExtractor(),
            ExtractorType.jhove,
            new JHoveExtractor()
        );

        for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
            Path directory = entry.getKey();
            logger.info("process directory '{}'", directory);
            for (Path filename : entry.getValue()) {
                Path filepath = directory.resolve(filename);
                logger.info("process file '{}'", filepath);

                Map<String, Object> propertyTree = new HashMap<>();
                for (ExtractorType extratorType : config.getExtraction().getModules()) {
                    logger.debug("apply extractor {}", extratorType);
                    propertyTree.put(extratorType.name(), extractors.get(extratorType).extractProperties(filepath));
                }

                List<FileRule> rules = config.getValidation().getImageRules();
                List<String> errors = FileRuleEvaluator.evaluateRules(rules, propertyTree);
                logger.info("validation of file '{}' {}", filepath, errors.isEmpty() ? "was successfull" : "failed");

                if (config.getOutput().getLogging().getJson()) {
                    JsonLogWriter.writeFileValidationLog(filepath, errors.isEmpty());
                }
            }

            logger.info("validation of directory '{}' was successful", directory);
            if (config.getOutput().getLogging().getJson()) {
                JsonLogWriter.writeDirectoryValidationLog(directory, true);
            }
        }

        logger.info("done validating all files");

        if (Objects.nonNull(config.getOutput().getCsv().getPath())) {
            logger.info("write csv output file '{}'", config.getOutput().getCsv().getPath());
            // CsvWriter.writeReport(filesByDirectory, extractionResultsByFile, outputOptions.csvOutputPath);
        }

        return 0;
    }
}
