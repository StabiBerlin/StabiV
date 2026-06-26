package de.spkberlin.stabiv.cli.commands;

import de.spkberlin.stabiv.cli.commands.options.CommonOptions;
import de.spkberlin.stabiv.cli.common.Logging;
import de.spkberlin.stabiv.cli.configuration.ConfigurationMerger;
import de.spkberlin.stabiv.cli.configuration.ConfigurationParser;
import de.spkberlin.stabiv.cli.configuration.ConfigurationValidator;
import de.spkberlin.stabiv.cli.configuration.ConfigurationWriter;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationLoggingSection;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationLoggingVerbosityType;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationPath;
import de.spkberlin.stabiv.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.stabiv.cli.extraction.ExtractorList;
import de.spkberlin.stabiv.cli.extraction.ExtractorType;
import de.spkberlin.stabiv.cli.output.report.CsvWriter;
import de.spkberlin.stabiv.cli.output.report.ExcelWriter;
import de.spkberlin.stabiv.cli.output.report.ReportGenerator;
import de.spkberlin.stabiv.cli.output.report.ReportRow;
import de.spkberlin.stabiv.cli.validation.ValidateAll;
import de.spkberlin.stabiv.cli.validation.ValidateAllResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
    name = "validate",
    description = """
    %nValidate all image files according to a specification.

    @|bold Examples|@

    Validate a single file and check whether JHove detects it as wellformed:%n
      ${COMMAND-FULL-NAME} --imageRule="$.jhove.wellformed/equal/true" /path/to/image.tif

    Validate all tif files in all subdirectories and check whether JHove detects them as wellformed:%n
      ${COMMAND-FULL-NAME} --imageRule="$.jhove.wellformed/equal/true" --include "**.tif" /path/to/directory

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

    @Spec
    CommandSpec spec;

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Configuration File Options|@%n%n", order = 2, exclusive = false)
    public ConfigOptions configOptions = new ConfigOptions();

    static class ConfigOptions {

        /** The filepath to the configuration file. */
        @Option(names = { "--config" }, paramLabel = "FILEPATH", description = "Load and use a configuration file")
        Path configFilepath;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Processing Options|@%n%n", order = 3, exclusive = false)
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

            Possible values are listed in the Java documentation:
            https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms
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

        /** The filepath to the exiftool binary. */
        @Option(
            names = { "--exiftoolPath" },
            paramLabel = "PATH",
            description = """
            The filepath to the exiftool binary (default "exiftool").
            """
        )
        Path exiftoolPath;

        /** The list of exiftool arguments. */
        @Option(
            names = { "--exiftoolArguments" },
            paramLabel = "ARGUMENTS",
            description = """
            The arguments provided to the exiftool command (default "-a -u -s -G5 -charset filename=utf8").

            The argument "-G" is mandatory such that hierarchical properties can be parsed from the output of exiftool.

            Arguments need to be enclosed in quotes, e.g., `--exiftoolArguments="-a -u -s -G5 -charset filename=utf8"`.
            """
        )
        List<String> exiftoolArguments;
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

            The expected value can be either a simple value or a list of values separated by comma.
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

            Examples
            - `$.exiftool..PhotometricInterpretation` - all images in a directory need to have the same color format
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

        /** The base directory for relative output filepaths. */
        @Option(
            names = { "--outputBaseDirectory" },
            arity = "1",
            paramLabel = "DIRECTORY",
            description = """
            The directory that is used as base directory when resolving relative filepaths for output options
            (in particular the CSV and Excel report filepath options). Defaults to the global base directory or the current working
            directory, if no global base directory is specified via the nameless INPUT argument.

            For example:
            - `/path/to/directory`
            """
        )
        Path baseDirectory;

        /** The filepath to the generated CSV file. */
        @Option(
            names = { "--checksumFilename" },
            arity = "1",
            paramLabel = "FILENAME",
            description = """
            The filename of a checksum file that is generated inside directories that have been succesfully validated.

            The filename may contain the following substitution variables:

            - `directory` - the name of the directory containing images
            - `date` - the current date in format `YY-MM-DD`
            - `time` - the current time in format `HH-MM-SS`

            On Linux, the parameter needs to be enclosed in single quotes if a variable is included to prevent that environment variables
            are substituted instead, e.g. `--checksum '${directory}.sha1'`.

            For example:
            - `checksum.sha1`
            - `${directory}.sha1`
            """
        )
        String checksumFilename;

        /** The filepath to the generated CSV file. */
        @Option(names = { "--csv" }, paramLabel = "FILEPATH", description = "CSV output file")
        String csvOutputFilepath;

        /** The filepath to the generated CSV file. */
        @Option(names = { "--excel" }, paramLabel = "FILEPATH", description = "Excel output file (xlsx)")
        String excelOutputFilepath;

        @Option(
            names = { "--reportColumn" },
            arity = "1",
            paramLabel = "COLUMN",
            description = """
            Include an image property as additional column in the validation report.

            Columns need to be specified in the format `"LABEL/PROPERTY"`.

            Examples:
              - `"JHove Wellformed/$.jhove.wellformed"`
              - `"Color Format/$.exiftool..PhotometricInterpretation"`

            Properties are selected by specifying a JsonPath, see `extract` subcommand or RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535).

            The label can be any text.
            """
        )
        List<String> reportColumns = Collections.emptyList();

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
            paramLabel = "PATTERN",
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

        @Option(
            names = { "--include" },
            paramLabel = "PATTERN",
            arity = "1",
            description = """
            one ore multiple files, directories or glob patterns that are validated

            Examples
            - `&ast;.tif` - validate all tif files in the base directory not considering subdirectories
            - `&ast;&ast;.tif` - validate all tif files in the base directory including all subdirectories
            - `&ast;&ast;/&ast;.{tif,jpg}` - validate tif and jpg files in all subdirectories, but not the base directory
            """
        )
        List<String> includes = Collections.emptyList();

        /** The list of directories and files that are being validated. */
        @Parameters(
            paramLabel = "INPUT",
            arity = "0..*",
            description = """
            Defines which images files are validated.

            If a single directory is provided as INPUT, it is interpreted as the base directory that is scanned for image files based on
            all defined include and exclude rules (either via command line arguments or via a configuration file). In all other cases,
            include and exclude rules are ignored. For example, if a single image file is provided as INPUT, it is considered the only file
            that needs to be evaluated in dependent of any other include or exclude rules. If multiple individual files or a filepath
            pattern is provided as INPUT, the matching images files are validated while ignoring any additional include and exclude rules.

            By default, if nothing is provided as INPUT, the current working directory is assumed as base directory and scanned for image
            files based on all include and exclude rules.

            If more specific base directories are defined, e.g. via `--inputBaseDirectory` or `--outputBaseDirectry`, then they will
            overwrite the base directory that is provided here (if it is specified).

            Examples
            - `/path/to/directory` - validate all files according to include and exclude rules in this directory
            - `/path/to/file.tif` - validate this single tif file only
            - `/path/to/000001.tif /path/to/000002.tif` - validate these two tif files only
            - `/path/to/*.tif` - validate all tif files in this directory, ignoring any include and exclude rules
            """
        )
        List<String> inputArguments = Collections.emptyList();
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(outputOptions.jsonLogging);
        Logging.setLogLevel(outputOptions.verbosity.length + 1);

        logger.info("run validate command");

        if (Objects.isNull(configOptions.configFilepath) && inputOptions.inputArguments.isEmpty()) {
            logger.error("you need to provide at least a config file or reference files as INPUT argument");
            CommandLine.usage(this, System.out);
            return 2;
        }

        Path configFilepath = null;
        if (Objects.nonNull(configOptions.configFilepath)) {
            configFilepath = Path.of(CWD).resolve(configOptions.configFilepath).normalize().toAbsolutePath();
        }

        ConfigurationRoot config;
        try {
            config = loadConfiguration(configFilepath);
        } catch (IOException e) {
            logger.error("configuration file {} could not be loaded, exiting ...", configFilepath.toString());
            return 2;
        }

        if (!ConfigurationValidator.validateConfiguration(config)) {
            logger.error("configuration invalid, exiting ...");
            return 2;
        }

        Logging.setLogMode(config.getOutput().getLogging().getJson());
        Logging.setLogLevel(ConfigurationLoggingSection.VERBOSITY_LEVELS.indexOf(config.getOutput().getLogging().getVerbosity()));

        ValidateAllResult result = ValidateAll.validateAll(config, spec.commandLine().getOut());
        writeReport(result, config, configFilepath);
        return ValidateAll.isOverallValid(result) ? 0 : 3;
    }

    /**
     * Generate and write the report as CSV and Excel files if configured.
     *
     * @param result the validation result for all directories and files
     * @param config the configuration
     * @param configFilepath filepath to the configuration file
     * @throws IOException in case writing the report fails
     */
    public void writeReport(ValidateAllResult result, ConfigurationRoot config, Path configFilepath) throws IOException {
        ConfigurationPath csvPath = config.getOutput().getReport().getCsv().getPath();
        ConfigurationPath excelPath = config.getOutput().getReport().getExcel().getPath();

        if (Objects.nonNull(csvPath) || Objects.nonNull(excelPath)) {
            // determine extractor versions
            Map<String, String> extractorVersions = new ExtractorList(
                config.getExtraction().getModules(),
                config.getExtraction().getExiftool()
            )
                .getImplementations()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getVersion()));

            // get original cli arguments list
            List<String> commandArgs = spec.commandLine().getParseResult().originalArgs();

            List<ReportRow> report = ReportGenerator.generateReport(
                result,
                config.getValidation().getImageRules(),
                config.getValidation().getDirectoryRules().getConsistentProperties(),
                config.getValidation().getDirectoryRules().getPropertyDeviations(),
                config.getOutput().getReport().getColumns(),
                commandArgs,
                configFilepath,
                extractorVersions
            );

            if (Objects.nonNull(csvPath)) {
                CsvWriter.writeReport(config.getOutput().getReport().getCsv(), report, config.getInput().getBaseDirectory());
            }

            if (Objects.nonNull(excelPath)) {
                ExcelWriter.writeReport(config.getOutput().getReport().getExcel(), report, config.getInput().getBaseDirectory());
            }
        }
    }

    /**
     * Load the main Yaml configuration (or generate a default configuration).
     *
     * @return the yaml configuration
     * @throws IOException in case filepaths cannot be evaluated
     */
    public ConfigurationRoot loadConfiguration(Path configFilepath) throws IOException {
        ConfigurationRoot config = new ConfigurationRoot();

        if (Objects.nonNull(configFilepath)) {
            logger.info("load configuration file '{}'", configFilepath);
            config = ConfigurationParser.parseConfigurationFile(configFilepath);
        }

        logger.debug("merge configuration with command line options");
        ConfigurationLoggingVerbosityType verbosity = ConfigurationLoggingSection.VERBOSITY_LEVELS.get(outputOptions.verbosity.length + 1);
        ConfigurationMerger.mergeInput(config, Path.of(CWD), inputOptions.inputArguments, inputOptions.includes, inputOptions.excludes);
        ConfigurationMerger.mergeProcessing(config, processingOptions.parallelThreads, processingOptions.checksumAlgorithm);
        ConfigurationMerger.mergeExtraction(
            config,
            extractionOptions.modules,
            extractionOptions.exiftoolPath,
            extractionOptions.exiftoolArguments
        );
        ConfigurationMerger.mergeValidation(
            config,
            validationOptions.imageRules,
            validationOptions.consistentProperties,
            validationOptions.consecutiveNumbering,
            validationOptions.propertyDeviations
        );
        ConfigurationMerger.mergeOutput(
            config,
            Path.of(CWD),
            inputOptions.inputArguments,
            outputOptions.baseDirectory,
            outputOptions.csvOutputFilepath,
            outputOptions.excelOutputFilepath,
            outputOptions.reportColumns,
            outputOptions.checksumFilename,
            outputOptions.jsonLogging,
            verbosity
        );

        logger.debug("yaml configuration after merging with command line options:\n\n{}", ConfigurationWriter.writeFile(config));
        return config;
    }
}
