package de.spkberlin.stabiv.cli.validation;

import de.spkberlin.stabiv.cli.configuration.state.ConfigurationRoot;
import de.spkberlin.stabiv.cli.extraction.ExtractorList;
import de.spkberlin.stabiv.cli.output.checksum.ChecksumFileWriter;
import de.spkberlin.stabiv.cli.output.json.JsonLogWriter;
import de.spkberlin.stabiv.cli.path.FileUtils;
import de.spkberlin.stabiv.common.validation.directories.DirectoryValidation;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryValidationResult;
import de.spkberlin.stabiv.common.validation.images.ImageValidation;
import de.spkberlin.stabiv.common.validation.images.state.ImageValidationResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateAll {

    private static final Logger logger = LoggerFactory.getLogger(ValidateAll.class);

    /**
     * Validate all files and directories given a full configuration of the CLI.
     *
     * @param config the full configuration for validating a set of image directories and files
     * @param stdout the stdout print writer to write json messages to
     * @return the validation result for all directories and images
     */
    public static ValidateAllResult validateAll(ConfigurationRoot config, PrintWriter stdout) {
        logger.debug("initialize {} threads for parallel image validation", config.getProcessing().getThreads());
        ExecutorService pool = Executors.newFixedThreadPool(config.getProcessing().getThreads());

        try {
            Map<Path, List<Path>> filesByDirectory = FileUtils.resolveFiles(config.getInput());
            Map<Path, ImageValidationResult> allImageValidationResults = new HashMap<>();
            Map<Path, DirectoryValidationResult> directoryValidationResults = new HashMap<>();

            for (Entry<Path, List<Path>> entry : filesByDirectory.entrySet()) {
                Path directory = entry.getKey();
                DirectoryValidationResult directoryResult = validateDirectory(entry.getKey(), entry.getValue(), config, pool, stdout);
                directoryValidationResults.put(directory, directoryResult);
                allImageValidationResults.putAll(directoryResult.getImageResults());
            }

            logger.info("done validating all files");
            return new ValidateAllResult(filesByDirectory, allImageValidationResults, directoryValidationResults);
        } catch (IOException e) {
            logger.error("failed to resolve files due to:", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            logger.error("error during image validation due to:", e.getCause());
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            logger.error("image validation was interrupted due to:", e);
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Validate a single directory.
     *
     * @param directory the directory filepath
     * @param files all image filepaths within that directory
     * @param config the global cli configuration
     * @param pool the executor pool
     * @param stdout the stdout printer
     * @return a directory validation result for the directory
     */
    public static DirectoryValidationResult validateDirectory(
        Path directory,
        List<Path> files,
        ConfigurationRoot config,
        ExecutorService pool,
        PrintWriter stdout
    ) throws IOException, ExecutionException, InterruptedException {
        DirectoryValidationResult directoryResult = DirectoryValidation.validate(
            directory,
            files,
            new ExtractorList(config.getExtraction().getModules()),
            config.getValidation().getImageRules(),
            config.getValidation().getDirectoryRules().getConsistentProperties(),
            config.getValidation().getDirectoryRules().getConsecutiveNumbering(),
            config.getValidation().getDirectoryRules().getPropertyDeviations(),
            config.getProcessing().getChecksumAlgorithm(),
            pool
        );

        if (config.getOutput().getLogging().getJson()) {
            for (ImageValidationResult imageResult : directoryResult.getImageResults().values()) {
                JsonLogWriter.writeFileValidationLog(imageResult, stdout);
            }
        }

        if (directoryResult.getDirectoryResult().getErrors().isEmpty()) {
            logger.info("validation of directory '{}' was successful", directory);
            ChecksumFileWriter.writeFile(
                directory,
                config.getOutput().getChecksum().getFilename(),
                config.getOutput().getChecksum().getLineFormat(),
                mapToChecksums(directoryResult)
            );
        }

        if (config.getOutput().getLogging().getJson()) {
            JsonLogWriter.writeDirectoryValidationLog(directoryResult, stdout);
        }

        return directoryResult;
    }

    /**
     * Return true if validation of directories and images was succesful overall.
     *
     * @param result the validation results for all directories and images
     * @return true if all were valid
     */
    public static boolean isOverallValid(ValidateAllResult result) {
        // no files is considered invalid
        if (result.getFilesByDirectory().isEmpty()) {
            return false;
        }

        // check all directory results
        for (DirectoryValidationResult directoryResult : result.getDirectoryValidationResults().values()) {
            if (!DirectoryValidation.isDirectoryValid(directoryResult)) {
                return false;
            }
        }

        // check all image results
        for (ImageValidationResult imageResult : result.getImageValidationResults().values()) {
            if (!ImageValidation.isImageValid(imageResult)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the total number of images that were validated, independent of the validation success.
     *
     * @param result the validation results for all directories and images
     * @return the total count of images
     */
    public static Integer countOverallImages(ValidateAllResult result) {
        return result.getFilesByDirectory().values().stream().mapToInt(List::size).sum();
    }

    /**
     * Return the total number of valid images.
     *
     * @param result the validation results for all directories and images
     * @return the total count of valid images
     */
    public static Long countValidImages(ValidateAllResult result) {
        return result.getImageValidationResults().values().stream().filter(ImageValidation::isImageValid).count();
    }

    private static Map<Path, String> mapToChecksums(DirectoryValidationResult result) {
        return result.getImageResults().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getChecksum()));
    }
}
