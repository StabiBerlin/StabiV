package de.spkberlin.imagevalidation.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.imagevalidation.cli.common.TestConstants;
import de.spkberlin.imagevalidation.cli.output.json.JsonLogEntry;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class ValidateCommandTest {

    private static final String NEWLINE = System.getProperty("line.separator");

    private static final Path EXAMPLE_PATH = TestConstants.EXAMPLES_PATH.resolve("simple/wellformed/simple_rgb.tif");
    private static final Path UNKNOWN_PATH = TestConstants.EXAMPLES_PATH.resolve("unknown.tif");

    private static final Path SIMPLE_CONFIG_PATH = TestConstants.EXAMPLES_PATH.resolve("simple/config.yaml");

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
     * Check that a single file can be validated for being wellformed accoring to JHove.
     */
    @Test
    public void validateSimpleTif() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute("-v", "--imageRule=$.jhove.wellformed/equal/true", EXAMPLE_PATH.toAbsolutePath().toString());
        assertEquals(0, exitCode);
    }

    /**
     * Check that a single file can be validated for being wellformed accoring to JHove.
     */
    @Test
    public void validateSimpleConfigExample() {
        CommandLine cmd = new CommandLine(new ValidateCommand());
        int exitCode = cmd.execute("-v", "--config=" + SIMPLE_CONFIG_PATH.toAbsolutePath().toString());
        assertEquals(3, exitCode);
    }

    /**
     * Check that a single file can be validated for being wellformed accoring to JHove.
     */
    @Test
    public void validateSpkBerlinConfigExample() throws JsonProcessingException, JsonMappingException {
        StringWriter stdout = new StringWriter();
        CommandLine cmd = new CommandLine(new ValidateCommand());
        cmd.setOut(new PrintWriter(stdout));
        int exitCode = cmd.execute("-v", "--json", "--config=" + SPK_BERLIN_CONFIG_PATH.toAbsolutePath().toString());
        assertEquals(3, exitCode);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<Path, Boolean> successByPath = new HashMap<>();
        for (String line : stdout.toString().split(NEWLINE)) {
            JsonLogEntry logEntry = objectMapper.readValue(line, JsonLogEntry.class);
            Path relativePath = SPK_BERLIN_DIRECTORY.toAbsolutePath().relativize(Path.of(logEntry.getPath()));
            successByPath.put(relativePath, JsonLogEntry.VALIDATION_SUCCESS.equals(logEntry.getValidation()));
        }

        // valid directories
        List<String> validDirectories = List.of("size_deviations_valid", "consistency_valid", "consecutive_numbering_valid");
        for (String validDirectory : validDirectories) {
            assertTrue(successByPath.get(Path.of(validDirectory)));
        }

        // invalid directories
        List<String> invalidDirectories = List.of(
            "size_deviations_first_slice_invalid",
            "size_deviations_last_slice_invalid",
            "size_deviations_center_slice_invalid",
            "image_rules_valid",
            "image_rules_invalid",
            "consistency_invalid",
            "consecutive_numbering_invalid"
        );
        for (String invalidDirectory : invalidDirectories) {
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
