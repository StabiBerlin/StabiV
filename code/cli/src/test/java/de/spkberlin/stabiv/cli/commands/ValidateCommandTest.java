package de.spkberlin.stabiv.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.stabiv.cli.commands.ValidateCommand;
import de.spkberlin.stabiv.cli.common.TestConstants;
import de.spkberlin.stabiv.cli.output.json.JsonLogEntry;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class ValidateCommandTest {

    @TempDir
    private Path tempDir;

    private static final String NEWLINE = System.getProperty("line.separator");

    private static final Path UNKNOWN_PATH = TestConstants.EXAMPLES_PATH.resolve("unknown");

    private static final Path SIMPLE_DIRECTORY = TestConstants.EXAMPLES_PATH.resolve("simple");
    private static final Path SIMPLE_CONFIG_PATH = SIMPLE_DIRECTORY.resolve("config.yaml");

    private static final Path SPK_BERLIN_DIRECTORY = TestConstants.EXAMPLES_PATH.resolve("spk-berlin");
    private static final Path SPK_BERLIN_CONFIG_PATH = SPK_BERLIN_DIRECTORY.resolve("config.yaml");

    /**
     * Check that providing a non-existing file returns an error code stating failed validation.
     */
    @Test
    public void noFilesToValidate() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(UNKNOWN_PATH.toAbsolutePath().toString());
        assertEquals(3, exitCode);
    }

    /**
     * Check that a single file can be validated by providing it as include argument.
     */
    @Test
    public void validateSimpleTifViaIncludeArgument() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--imageRule=$.jhove.wellformed/equal/true",
            "--include=wellformed/simple_rgb.tif",
            SIMPLE_DIRECTORY.toString()
        );
        assertEquals(0, exitCode);
    }

    /**
     * Check that a single file can be validated by providing it as nameless input argument.
     */
    @Test
    public void validateSingleTifViaNamelessArgument() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--imageRule=$.jhove.wellformed/equal/true",
            SIMPLE_DIRECTORY.resolve("wellformed/simple_rgb.tif").toString()
        );
        assertEquals(0, exitCode);
    }

    /**
     *
     * Check that multiple individual file can be validated by providing them as nameless input arguments.
     */
    @Test
    public void validateMultipleTifViaNamelessArgument() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--imageRule=$.jhove.wellformed/equal/true",
            SIMPLE_DIRECTORY.resolve("wellformed/simple_rgb.tif").toString(),
            SIMPLE_DIRECTORY.resolve("wellformed/simple_grayscale.tif").toString()
        );
        assertEquals(0, exitCode);
    }

    /**
     * Check that files matching a file pattern can be validated by providing the pattern as nameless input argument.
     */
    @Test
    public void validateTifPatternAsNamelessArgument() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--imageRule=$.jhove.wellformed/equal/true",
            SIMPLE_DIRECTORY.resolve("wellformed").toString() + "/*.tif"
        );
        assertEquals(0, exitCode);
    }

    /**
     * Check that a directory can be validated with a simple configuation file.
     */
    @Test
    public void validateSimpleConfigExample() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--config=" + SIMPLE_CONFIG_PATH.toAbsolutePath().toString(),
            "--outputBaseDirectory=" + tempDir,
            SIMPLE_DIRECTORY.toString()
        );
        assertEquals(3, exitCode);
    }

    /**
     * Check that the SPK-Berlin example configuration successfully validates a specific directory provided as nameless input argument.
     */
    @Test
    void validateSpkBerlinSingleValidDirectory() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--config=" + SPK_BERLIN_CONFIG_PATH.toAbsolutePath().toString(),
            "--outputBaseDirectory=" + tempDir,
            SPK_BERLIN_DIRECTORY.resolve("consecutive_numbering_valid").toString()
        );
        assertEquals(0, exitCode);
    }

    /**
     * Check that the SPK-Berlin example configurations fails to validate for a specific directory provided as nameless input argument.
     */
    @Test
    void validateSpkBerlinSingleInvalidDirectory() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute(
            "-v",
            "--config=" + SPK_BERLIN_CONFIG_PATH.toAbsolutePath().toString(),
            "--outputBaseDirectory=" + tempDir,
            SPK_BERLIN_DIRECTORY.resolve("consistency_invalid").toString()
        );
        assertEquals(3, exitCode);
    }

    /**
     * Check that the SPK-Berlin example configuration correctly fails and succeeds for all example directories.
     */
    @Test
    public void validateSpkBerlinConfigExample() throws JsonProcessingException, JsonMappingException {
        StringWriter stdout = new StringWriter();
        CommandLine cmd = new CommandLine(new ValidateCommand());
        cmd.setOut(new PrintWriter(stdout));
        int exitCode = cmd.execute(
            "-v",
            "--json",
            "--config=" + SPK_BERLIN_CONFIG_PATH.toAbsolutePath().toString(),
            "--outputBaseDirectory=" + tempDir,
            SPK_BERLIN_DIRECTORY.toString()
        );
        assertEquals(3, exitCode);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<Path, Boolean> successByPath = new HashMap<>();
        for (String line : stdout.toString().split(NEWLINE)) {
            JsonLogEntry logEntry = objectMapper.readValue(line, JsonLogEntry.class);

            Path relativePath = SPK_BERLIN_DIRECTORY.toAbsolutePath().relativize(Path.of(logEntry.getPath()));
            successByPath.put(relativePath, JsonLogEntry.VALIDATION_SUCCESS.equals(logEntry.getValidation()));
        }

        // valid directories
        List<String> validDirectories = List.of(
            "size_deviations_valid",
            "consistency_valid",
            "consecutive_numbering_valid",
            "size_deviations_center_slice_invalid",
            "utf8😜_filepath_valid"
        );
        for (String validDirectory : validDirectories) {
            assertTrue(successByPath.get(Path.of(validDirectory)));
        }

        // invalid directories
        List<String> invalidDirectories = List.of(
            "size_deviations_first_slice_invalid",
            "size_deviations_last_slice_invalid",
            "image_rules_valid",
            "image_rules_invalid",
            "consistency_invalid",
            "consecutive_numbering_invalid"
        );
        for (String invalidDirectory : invalidDirectories) {
            System.out.println(invalidDirectory);
            assertFalse(successByPath.get(Path.of(invalidDirectory)));
        }

        // directories that only contain valid image files
        for (Map.Entry<Path, Boolean> entry : successByPath.entrySet()) {
            if (entry.toString().endsWith(".tif")) {
                if (entry.toString().startsWith("image_rules_invalid")) {
                    // all these files are individually invalid
                    assertFalse(successByPath.get(entry.getKey()));
                } else {
                    // all these files are individually valid
                    assertTrue(successByPath.get(entry.getKey()));
                }
            }
        }
    }
}
