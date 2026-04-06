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
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Map<Path, List<Path>> filesByDirectory = FileUtils.resolveFiles(config.getInput());
            Map<Path, ImageValidationResult> allImageValidationResults = new HashMap<>();
            Map<Path, DirectoryValidationResult> directoryValidationResults = new HashMap<>();

            for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
                Path directory = entry.getKey();
                logger.info("process directory '{}'", directory);

                Map<Path, ImageValidationResult> imageValidationResultsByDirectory = new HashMap<>();
                List<Future<ImageValidationResult>> resultFutures = pool.invokeAll(createImageValidationTasks(entry.getValue(), config));

                for (Future<ImageValidationResult> resultFuture : resultFutures) {
                    ImageValidationResult result = resultFuture.get();

                    if (config.getOutput().getLogging().getJson()) {
                        JsonLogWriter.writeFileValidationLog(result);
                    }

                    Path filepath = result.getFilePath();
                    imageValidationResultsByDirectory.put(filepath, result);
                }

                DirectoryValidationResult directoryResult = DirectoryValidation.validate(directory, imageValidationResultsByDirectory);
                directoryValidationResults.put(directory, directoryResult);
                allImageValidationResults.putAll(imageValidationResultsByDirectory);

                if (config.getOutput().getLogging().getJson()) {
                    JsonLogWriter.writeDirectoryValidationLog(directoryResult);
                }
            }

            logger.info("done validating all files");
            return new ValidateAllResult(filesByDirectory, allImageValidationResults);
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
}
