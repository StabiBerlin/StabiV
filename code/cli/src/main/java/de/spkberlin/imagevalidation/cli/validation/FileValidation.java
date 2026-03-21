package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.cli.commands.ValidateCommand;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationLoggingSection;
import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.cli.output.json.JsonLogWriter;
import de.spkberlin.imagevalidation.cli.path.FileUtils;
import de.spkberlin.imagevalidation.cli.path.GlobResolver;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.rules.images.ImageRuleEvaluator;
import de.spkberlin.imagevalidation.common.rules.images.state.ImageRule;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ValidateCommand.class);

    /**
     * Find all files and directories that need to be validated.
     *
     * @param config the main yaml configuration
     * @return all directories and their files that need to be validated
     * @throws IOException in case filepaths cannot be evaluated
     */
    public static Map<Path, List<Path>> resolveFiles(ConfigurationRoot config) throws IOException {
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
    public static Integer doValidation(ConfigurationRoot config) throws IOException {
        Logging.setLogMode(config.getOutput().getLogging().getJson());
        Logging.setLogLevel(ConfigurationLoggingSection.VERBOSITY_LEVELS.indexOf(config.getOutput().getLogging().getVerbosity()));

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

                List<ImageRule> rules = config.getValidation().getImageRules();
                List<String> errors = ImageRuleEvaluator.evaluateRules(rules, propertyTree);
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
