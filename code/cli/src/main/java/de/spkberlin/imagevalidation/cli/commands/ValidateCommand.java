package de.spkberlin.imagevalidation.cli.commands;

import de.spkberlin.imagevalidation.cli.common.Constants;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.output.csv.CsvWriter;
import de.spkberlin.imagevalidation.cli.path.FileUtils;
import de.spkberlin.imagevalidation.cli.path.GlobResolver;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = Constants.CLI_COMMAND_NAME,
    mixinStandardHelpOptions = true, // automatically add help option
    showAtFileInUsageHelp = true, // allow to provide list of directories and files as a file instead of as cli arguments
    version = Constants.VERSION,
    description = "Validates all images inside directories given the provided configuration file."
)
public class ValidateCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ValidateCommand.class);

    /** The filepath to the configuration file. */
    @Option(names = { "-c", "--config" }, description = "Configuration file (default \"validationConfig.yaml\")")
    private File configFile = new File("./validationConfig.yaml");

    /** The filepath to the generated CSV file. */
    @Option(names = { "--csv" }, description = "CSV output file (default \"validationResults.csv\")")
    private Path csvOutputPath = Path.of("./validationResults.csv");

    /** Wether to use json based log output. */
    @Option(names = { "-j", "--json" }, description = "print logging in JSON format")
    private boolean jsonLogging = false;

    /** Wether to show additional log messages. */
    @Option(names = { "-v", "--verbose" }, description = "print more logging")
    private boolean verbose = false;

    /** The list of directories and files that are being validated. */
    @Parameters(paramLabel = "PATHS", arity = "1..*", description = "directories or individual files to validate as glob pattern")
    private Path[] pathArgs;

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(jsonLogging);
        Logging.setLogLevel(verbose);

        logger.info("run validate command");

        logger.debug(
            "filepath pattern arguments are: {}",
            Arrays.stream(pathArgs)
                .map(p -> p.toString())
                .collect(Collectors.joining(", "))
        );

        List<Path> resolvedFiles = GlobResolver.resolveMany(Arrays.asList(pathArgs));

        Map<Path, List<Path>> filesByDirectory = FileUtils.mapFilesByDirectory(resolvedFiles);

        for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
            logger.info("process directory '{}'", entry.getKey());
            for (Path filename : entry.getValue()) {
                logger.info("process file '{}'", filename);
            }
        }

        if (Objects.nonNull(csvOutputPath)) {
            logger.info("write CSV file");
            CsvWriter.writeReport(filesByDirectory, csvOutputPath);
        }

        return 0;
    }
}
