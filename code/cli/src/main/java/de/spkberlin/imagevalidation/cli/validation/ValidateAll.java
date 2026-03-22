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

    public static ValidateAllResult validateAll(ConfigurationRoot config) {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Map<Path, List<Path>> filesByDirectory = FileUtils.resolveFiles(config.getInput());
            Map<Path, ImageValidationResult> imageValidationResults = new HashMap<>();

            for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
                Path directory = entry.getKey();
                logger.info("process directory '{}'", directory);

                List<Future<ImageValidationResult>> resultFutures = pool.invokeAll(createImageValidationTasks(entry.getValue(), config));

                for (Future<ImageValidationResult> resultFuture : resultFutures) {
                    ImageValidationResult result = resultFuture.get();
                    Path filepath = result.getFilePath();

                    if (config.getOutput().getLogging().getJson()) {
                        JsonLogWriter.writeFileValidationLog(filepath, result);
                    }

                    imageValidationResults.put(filepath, result);
                }

                logger.info("validation of directory '{}' was ?????", directory);
                if (config.getOutput().getLogging().getJson()) {
                    JsonLogWriter.writeDirectoryValidationLog(directory, true);
                }
            }

            logger.info("done validating all files");

            return new ValidateAllResult(filesByDirectory, imageValidationResults);
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

    public static List<ImageValidationRunner> createImageValidationTasks(List<Path> filepaths, ConfigurationRoot config) {
        return filepaths
            .stream()
            .map(filepath ->
                new ImageValidationRunner(filepath, config.getExtraction().getModules(), config.getValidation().getImageRules())
            )
            .toList();
    }
}
