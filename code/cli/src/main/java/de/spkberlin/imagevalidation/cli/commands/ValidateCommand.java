package de.spkberlin.imagevalidation.cli.commands;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlConfiguration;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlParser;
import de.spkberlin.imagevalidation.cli.configuration.yaml.YamlWriter;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.cli.output.csv.CsvWriter;
import de.spkberlin.imagevalidation.cli.output.json.JsonLogWriter;
import de.spkberlin.imagevalidation.cli.path.FileUtils;
import de.spkberlin.imagevalidation.cli.path.GlobResolver;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "validate",
    description = "%nValidate all image files according to a specification.",
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

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Logging Options|@%n%n", order = 4, exclusive = false)
    private LoggingOptions loggingOptions = new LoggingOptions();

    static class LoggingOptions {

        /** Wether to use json based log output. */
        @Option(names = { "--json" }, description = "print validation results in JSON format to stdout (and other log messages to stderr)")
        boolean jsonLogging;

        /** Wether to show additional log messages. */
        @Option(names = { "-v", "--verbose" }, description = "print more logging (use `-vv` for debug logging or `-vvv` for trace logging)")
        boolean[] verbosity;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Configuration Options|@%n%n", order = 2, exclusive = false)
    public ConfigOptions configOptions = new ConfigOptions();

    static class ConfigOptions {

        /** The filepath to the configuration file. */
        @Option(names = { "--config" }, description = "Configuration file containing additional options")
        Path configFile;

        /** Which extraction modules to apply */
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
        Path csvOutputPath;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Input Options|@%n%n", order = 1, exclusive = false)
    public InputOptions inputOptions = new InputOptions();

    static class InputOptions {

        /** The list of directories and files that are being validated. */
        @Parameters(paramLabel = "PATHS", arity = "0..*", description = "directories or individual files to validate as glob pattern")
        Path[] pathArgs = {};
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(loggingOptions.jsonLogging);
        Logging.setLogLevel(Objects.isNull(loggingOptions.verbosity) ? 0 : loggingOptions.verbosity.length);

        logger.info("run validate command");

        if (Objects.nonNull(configOptions.configFile)) {
            logger.info("load configuration file '{}", configOptions.configFile);
            YamlConfiguration config = YamlParser.parseConfigurationFile(configOptions.configFile);
            logger.trace("read yaml configuration:\n\n{}", YamlWriter.writeConfigurationFile(config));
        }

        logger.debug(
            "filepath pattern arguments are: {}",
            Arrays.stream(inputOptions.pathArgs)
                .map(p -> p.toString())
                .collect(Collectors.joining(", "))
        );

        List<Path> resolvedFiles = GlobResolver.resolveMany(Arrays.asList(inputOptions.pathArgs));

        if (resolvedFiles.isEmpty()) {
            logger.warn("no files to validate");
        }

        Map<Path, List<Path>> filesByDirectory = FileUtils.mapFilesByDirectory(resolvedFiles);
        // Map<Path, ExtractionResult> extractionResultsByFile = new HashMap<>();

        for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
            Path directory = entry.getKey();
            logger.info("process directory '{}'", directory);
            for (Path filename : entry.getValue()) {
                Path filepath = directory.resolve(filename);
                logger.info("process file '{}'", filepath);

                for (ExtractorType extratorType : configOptions.extractors) {
                    logger.debug("apply extractor {}", extratorType);

                    if (extratorType.equals(ExtractorType.exiftool)) {
                        Extractor extractor = new ExiftoolExtractor();
                        // ExtractionResult extractionResult = extractor.extractProperties(filepath);
                        // extractionResultsByFile.put(filepath, extractionResult);

                        // logger.info("extraction result: {}", extractionResult);
                    }
                }

                logger.info("validation of file '{}' was successful", filepath);
                if (loggingOptions.jsonLogging) {
                    JsonLogWriter.writeFileValidationLog(filepath, true);
                }
            }

            logger.info("validation of directory '{}' was successful", directory);
            if (loggingOptions.jsonLogging) {
                JsonLogWriter.writeDirectoryValidationLog(directory, true);
            }
        }

        logger.info("done validating all files");

        if (Objects.nonNull(outputOptions.csvOutputPath)) {
            logger.info("write csv output file '{}'", outputOptions.csvOutputPath);
            // CsvWriter.writeReport(filesByDirectory, extractionResultsByFile, outputOptions.csvOutputPath);
        }

        return 0;
    }
}
