package de.spkberlin.imagevalidation.cli.commands;

import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.configuration.ConfigurationMerger;
import de.spkberlin.imagevalidation.cli.configuration.ConfigurationParser;
import de.spkberlin.imagevalidation.cli.configuration.ConfigurationValidator;
import de.spkberlin.imagevalidation.cli.configuration.ConfigurationWriter;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationLoggingSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationLoggingVerbosityType;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationPath;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.imagevalidation.cli.extraction.ExtractorType;
import de.spkberlin.imagevalidation.cli.output.csv.CsvWriter;
import de.spkberlin.imagevalidation.cli.path.GlobResolver;
import de.spkberlin.imagevalidation.cli.validation.ValidateAll;
import de.spkberlin.imagevalidation.cli.validation.ValidateAllResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
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
      ${COMMAND-FULL-NAME} --imageRule="$.jhove.wellformed/equal/true" /path/to/images/**/*.tif

    Use a config file to define all validation options:%n
      ${COMMAND-FULL-NAME} --config /path/to/config.yaml
    """,
    showAtFileInUsageHelp = true, // allow to provide list of directories and files as a file instead of as cli arguments
    optionListHeading = "%n@|bold General Options|@%n%n",
    exitCodeOnExecutionException = 1,
    exitCodeOnInvalidInput = 2,
    exitCodeOnSuccess = 0,
    exitCodeOnUsageHelp = 0,
    exitCodeOnVersionHelp = 0,
    exitCodeListHeading = "%n@|bold Exit Codes|@%n%n",
    exitCodeList = {
        "0:successful validation of all images and directories without any errors",
        "1:generic program error, validation was aborted",
        "2:an unknown CLI option or invalid parameter was specified",
        "3:no program errors, but validation failed for at least one image or directory with an error",
    }
)
public class ValidateCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ValidateCommand.class);

    private static final String CWD = System.getProperty("user.dir");

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Configuration File Options|@%n%n", order = 2, exclusive = false)
    public ConfigOptions configOptions = new ConfigOptions();

    static class ConfigOptions {

        /** The filepath to the configuration file. */
        @Option(names = { "--config" }, description = "Load and use a configuration file")
        String configFilepath;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Processing Options|@%n%n", order = 2, exclusive = false)
    public ProcessingOptions processingOptions = new ProcessingOptions();

    static class ProcessingOptions {

        @Option(
            names = { "--threads" },
            arity = "1",
            paramLabel = "COUNT",
            description = """
            The number of execution threads that are used to validate images in parallel (default 4).
            """
        )
        Integer parallelThreads;

        @Option(
            names = { "--checksumAlgorithm" },
            arity = "1",
            paramLabel = "ALGORITHM",
            description = """
            The checksum algorithm that is used to calculate image file checksums (default SHA-1).
            """
        )
        String checksumAlgorithm;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Extraction Options|@%n%n", order = 4, exclusive = false)
    public ExtractionOptions extractionOptions = new ExtractionOptions();

    static class ExtractionOptions {

        /** The extraction modules that will be applied. */
        @Option(
            names = { "--extractor" },
            paramLabel = "MODULE",
            description = """
            Extraction module that is applied to each image file.

            Possible values:
              - `jhove` - extract image properties via JHove, see https://jhove.openpreservation.org/
              - `exiftool` - extract image properties via Exiftool, see https://exiftool.org/
            """
        )
        List<ExtractorType> modules = List.of(ExtractorType.exiftool, ExtractorType.jhove);
    }

    @ArgGroup(validate = true, heading = "%n@|bold Validation Options|@%n%n", order = 5, exclusive = false)
    public ValidationOptions validationOptions = new ValidationOptions();

    static class ValidationOptions {

        @Option(
            names = { "--imageRule" },
            paramLabel = "RULE",
            arity = "1",
            description = """
            Add a validation rule that is checked against extracted image properties

            Image validation rules need to be specified in the format `"PROPERTY/ASSERT/VALUE"`.

            Examples
              - `"$.jhove.wellformed/equal/true"` - check that JHove detects file as wellformed
              - `"$.exiftool..PhotometricInterpretation/oneOf/RGB,BlackIsZero"` - image must be either RGB or grayscale
              - `"$.exiftool.TIFF.IFD/occurrences/1"` - tif image must contain exactly one IFD
              - `"$.exiftool.TIFF.IFD[0].SubfileType/equal/Full-resolution image"` - first sub-image must be a full-resolution image

            Properties are selected by specifying a JsonPath, see `extract` subcommand or RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535).

            The assert option defines how properties are checked against the expected value:
              - `equal` - checks that values are exactly the same
              - `oneOf` - checks that the extracted value matches one of the provided alternatives
              - `occurrences` - checks the number of times a certain property occurres in the image
              - `lowerThan`, `lowerThanOrEqual`, `greaterThan`, `greaterThanOrEqual` - compares the property to a number
              - `isInteger` - checks that the property is an integer

            The expected value can be either a simple value, or a list of values separated by comma.
            """
        )
        List<String> imageRules = Collections.emptyList();

        @Option(
            names = { "--consistentProperty" },
            paramLabel = "PROPERTY",
            arity = "1",
            description = """
            Add a validation requirement that a certain property needs to be consistent across all images of a directory

            Properties need to be specified a JsonPath, see `extract` subcommand or RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535).
            """
        )
        List<String> consistentProperties = Collections.emptyList();

        @Option(
            names = { "--consecutiveNumbering" },
            paramLabel = "REGEXP",
            arity = "1",
            description = """
            Add the validation requirement that a directory is supposed to only contain files with numbers in consecutive order from 1 to n.

            The regular expression needs to match filenames and capture the part of the filename that contains a number with a match group.

            Example:
             - `"^(\\d{6})\\.tif$"` - tif files with 6-digit numbers, e.g. `000001.tif`, `000002.tif`, etc.
            """
        )
        String consecutiveNumbering;

        @Option(
            names = { "--propertyDeviation" },
            paramLabel = "RULE",
            arity = "1",
            description = """
            Add a validation requirement that a number-based property may only deviate by a maximum amount from the average over all
            files in a directory.

            The validation rule needs to be defined in the format of `"LABEL/PROPERTY/DEVIATION"`.
            Additional options can only be specified in the YAML configuration file (e.g. whether to use the average value or median value).

            The label used to generate appropriate error messages in case the maximum deviation is exceeded.

            Properties are selected by specifying a JsonPath, see `extract` subcommand or RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535).

            The maximum deviation needs to be specified as a simple number.

            Example:
             - `"ImageWidth/$.exiftool.TIFF.IFD[0].ImageWidth/10"` - check that image width deviates at most 10 pixels from average
             - `"ImageHeight/$.exiftool.TIFF.IFD[0].ImageHeight/10"` - check that image height deviates at most 10 pixels from the average
            """
        )
        List<String> propertyDeviations = Collections.emptyList();
    }

    @ArgGroup(validate = true, heading = "%n@|bold Output Options|@%n%n", order = 6, exclusive = false)
    public OutputOptions outputOptions = new OutputOptions();

    static class OutputOptions {

        /** The filepath to the generated CSV file. */
        @Option(
            names = { "--checksumFilename" },
            arity = "1",
            paramLabel = "FILENAME",
            description = """
            The filename of a checksum file that is generated inside directories that have been succesfully validated.

            The filename may contain the following substitution variables:

            - `directory`

            On Linux, the parameter needs to be enclosed in single quotes if a variable is included to prevent that environment variables
            are substituted instead, e.g. `--checksum '${directory}.sha1'`.

            For example:
            - `checksum.sha1`
            - `${directory}.sha1`
            """
        )
        String checksumFilename;

        /** The filepath to the generated CSV file. */
        @Option(names = { "--csv" }, description = "CSV output file")
        String csvOutputFilepath;

        @Option(
            names = { "--csvColumn" },
            arity = "1",
            paramLabel = "COLUMN",
            description = """
            Include an image property as column in the csv output file.

            Columns need to be specified in the format `"LABEL/PROPERTY"`.

            Examples:
              - `"JHove Wellformed/$.jhove.wellformed"`
              - `"Color Format/$.exiftool..PhotometricInterpretation"`

            Properties are selected by specifying a JsonPath, see `extract` subcommand or RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535).

            A label can be any text.
            """
        )
        List<String> csvColumns = Collections.emptyList();

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
            names = { "--exclude" },
            paramLabel = "PATH",
            arity = "1",
            description = """
            one ore multiple files, directories or glob patterns that are excluded from validation

            Examples
            - `&ast;&ast;/ignore` - do not validate files or subdirectories called "ignore"
            - `&ast;&ast;.jpg` - do not validate jpg files
            - `/path/to/directory` - do not validate files in this specific directory
            """
        )
        List<String> excludes = Collections.emptyList();

        /** The list of directories and files that are being validated. */
        @Parameters(
            paramLabel = "PATHS",
            arity = "0..*",
            description = """
            one or multiple files, directories or glob patterns that are scanned for files to validate

            Examples
            - `/path/to/directory` - validate all files from all subdirectories
            - `/path/to/directory/&ast;.tif` - validate tif files in this directory not considering subdirectories
            - `/path/to/directory/&ast;&ast;/&ast;.tif` - validate tif files in all subdirectories
            - `/path/to/directory/&ast;&ast;/&ast;.{tif,jpg}` - validate tif and jpg files in all subdirectories
            - `../relative/path/to/directory` - relative path with respect to current working directory
            """
        )
        List<String> includes = Collections.emptyList();
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(outputOptions.jsonLogging);
        Logging.setLogLevel(outputOptions.verbosity.length + 1);

        logger.info("run validate command");

        ConfigurationRoot config;
        try {
            config = loadConfiguration();
        } catch (IOException e) {
            logger.error("configuration file could not be loaded, exiting ...");
            return 2;
        }

        if (!ConfigurationValidator.validateConfiguration(config)) {
            logger.error("configuration invalid, exiting ...");
            return 2;
        }

        Logging.setLogMode(config.getOutput().getLogging().getJson());
        Logging.setLogLevel(ConfigurationLoggingSection.VERBOSITY_LEVELS.indexOf(config.getOutput().getLogging().getVerbosity()));

        ValidateAllResult result = ValidateAll.validateAll(config);

        ConfigurationPath csvPath = config.getOutput().getCsv().getPath();
        if (Objects.nonNull(csvPath)) {
            Path resolvedCsvPath = csvPath.getBaseDirectory().resolve(csvPath.getPath()).normalize();
            CsvWriter.writeReport(resolvedCsvPath, result, config.getOutput().getCsv().getColumns());
        }

        return ValidateAll.isOverallValid(result) ? 0 : 3;
    }

    /**
     * Load the main Yaml configuration (or generate a default configuration).
     *
     * @return the yaml configuration
     * @throws IOException in case filepaths cannot be evaluated
     */
    public ConfigurationRoot loadConfiguration() throws IOException {
        ConfigurationRoot config = new ConfigurationRoot();

        Path cwd = Path.of(CWD).normalize().toAbsolutePath();
        Path configPath = null;

        if (Objects.nonNull(configOptions.configFilepath)) {
            logger.info("load configuration file '{}'", configOptions.configFilepath);
            configPath = Path.of(configOptions.configFilepath).normalize().toAbsolutePath();
            config = ConfigurationParser.parseConfigurationFile(configPath);
        }

        List<String> absoluteIncludes = inputOptions.includes
            .stream()
            .map(p -> GlobResolver.makePatternAbsolute(p, cwd))
            .toList();

        List<String> absoluteExcludes = inputOptions.excludes
            .stream()
            .map(p -> GlobResolver.makePatternAbsolute(p, cwd))
            .toList();

        logger.debug("merge configuration with command line options");
        ConfigurationLoggingVerbosityType verbosity = ConfigurationLoggingSection.VERBOSITY_LEVELS.get(outputOptions.verbosity.length + 1);
        ConfigurationMerger.mergeInput(config, cwd, configPath, absoluteIncludes, absoluteExcludes);
        ConfigurationMerger.mergeProcessing(config, processingOptions.parallelThreads, processingOptions.checksumAlgorithm);
        ConfigurationMerger.mergeExtraction(config, extractionOptions.modules);
        ConfigurationMerger.mergeValidation(
            config,
            validationOptions.imageRules,
            validationOptions.consistentProperties,
            validationOptions.consecutiveNumbering,
            validationOptions.propertyDeviations
        );
        ConfigurationMerger.mergeOutput(
            config,
            configPath,
            cwd,
            outputOptions.csvOutputFilepath,
            outputOptions.csvColumns,
            outputOptions.checksumFilename,
            outputOptions.jsonLogging,
            verbosity
        );

        logger.debug("yaml configuration after merging with command line options:\n\n{}", ConfigurationWriter.writeFile(config));
        return config;
    }
}
