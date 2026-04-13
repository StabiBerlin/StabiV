package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.imagevalidation.cli.output.json.JsonLogWriter;
import de.spkberlin.imagevalidation.cli.path.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateAll {

    private static final Logger logger = LoggerFactory.getLogger(ValidateAll.class);

    /**
     * Validate all files and directories given a full configuration of the CLI.
     *
     * @param config the full configuration for validating a set of image directories and files
     * @return the validation result for all directories and images
     */
    public static ValidateAllResult validateAll(ConfigurationRoot config) {
        logger.debug("initialize {} threads for parallel image validation", config.getValidation().getParallelThreads());
        ExecutorService pool = Executors.newFixedThreadPool(config.getValidation().getParallelThreads());

        try {
            Map<Path, List<Path>> filesByDirectory = FileUtils.resolveFiles(config.getInput());
            Map<Path, ImageValidationResult> allImageValidationResults = new HashMap<>();
            Map<Path, DirectoryValidationResult> directoryValidationResults = new HashMap<>();

            for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
                Path directory = entry.getKey();
                DirectoryValidationResult directoryResult = validateDirectory(entry.getKey(), entry.getValue(), config, pool);
                directoryValidationResults.put(directory, directoryResult);
                allImageValidationResults.putAll(directoryResult.getImageResults());
            }

            logger.info("done validating all files");
            return new ValidateAllResult(filesByDirectory, allImageValidationResults, directoryValidationResults);
        } catch (IOException e) {
            logger.error("failed to resolve files due to:", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            logger.error("failed to start image validation due to:", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("image validation was interrupted due to:", e);
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Validate all files of a single directory given a full configuration and an already initialized thread pool.
     *
     * @param directory the directory to be validated
     * @param filepaths the image files inside this directory to be validated
     * @param config the full configuration
     * @param pool an already initialized thread pool
     * @return validation results for the directory and all images
     * @throws ExecutionException in case there is an error while executing a validation task
     * @throws InterruptedException in case execution is interrupted
     */
    public static DirectoryValidationResult validateDirectory(
        Path directory,
        List<Path> filepaths,
        ConfigurationRoot config,
        ExecutorService pool
    ) throws IOException, ExecutionException, InterruptedException {
        logger.info("process directory '{}'", directory);

        Map<Path, ImageValidationResult> imageValidationResultsByDirectory = new HashMap<>();
        List<Future<ImageValidationResult>> resultFutures = pool.invokeAll(createImageValidationTasks(filepaths, config));

        for (Future<ImageValidationResult> resultFuture : resultFutures) {
            ImageValidationResult result = resultFuture.get();

            if (config.getOutput().getLogging().getJson()) {
                JsonLogWriter.writeFileValidationLog(result);
            }

            Path filepath = result.getFilePath();
            imageValidationResultsByDirectory.put(filepath, result);
        }

        DirectoryValidationResult directoryResult = DirectoryValidation.validate(
            directory,
            config.getValidation().getDirectoryRules().getConsistentProperties(),
            config.getValidation().getDirectoryRules().getConsecutiveNumbering(),
            imageValidationResultsByDirectory
        );

        if (config.getOutput().getLogging().getJson()) {
            JsonLogWriter.writeDirectoryValidationLog(directoryResult);
        }

        return directoryResult;
    }

    /**
     * Create image validation tasks to be run in parallel from a pool of processing threads.
     *
     * @param filepaths the filepaths of images to be validated
     * @param config the full configuration of the CLI
     * @return a list of image validation tasks to be queued for execution
     */
    public static List<ImageValidationRunner> createImageValidationTasks(List<Path> filepaths, ConfigurationRoot config) {
        return filepaths
            .stream()
            .map(filepath ->
                new ImageValidationRunner(filepath, config.getExtraction().getModules(), config.getValidation().getImageRules())
            )
            .toList();
    }

    /**
     * Return true if validation of directories and images was succesful overall.
     *
     * @param result the validation results for all directories and images
     * @return true if all were valid
     */
    public static boolean isOverallValid(ValidateAllResult result) {
        // check all directory results
        for (DirectoryValidationResult directoryResult : result.getDirectoryValidationResults().values()) {
            if (Objects.nonNull(directoryResult.getError())) {
                return false;
            }
        }

        // check all image results
        for (ImageValidationResult imageResult : result.getImageValidationResults().values()) {
            if (
                Objects.nonNull(imageResult.getImageRuleEvaluationResult().getErrors()) &&
                imageResult.getImageRuleEvaluationResult().getErrors().size() > 0
            ) {
                return false;
            }
        }
        return true;
    }
}
