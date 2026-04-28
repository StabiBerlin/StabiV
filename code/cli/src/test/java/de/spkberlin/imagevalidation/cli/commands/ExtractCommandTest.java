package de.spkberlin.imagevalidation.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.imagevalidation.cli.common.TestConstants;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class ExtractCommandTest {

    private static final Path EXAMPLE_PATH = TestConstants.EXAMPLES_PATH.resolve("simple/wellformed/simple_rgb.tif");

    /**
     * Check that image properties are extracted from a simple tif image.
     *
     * @throws JsonMappingException in case json parsing fails
     * @throws JsonProcessingException in case json parsing fails
     */
    @SuppressWarnings("unchecked")
    @Test
    public void extractBaseTif() throws JsonMappingException, JsonProcessingException {
        Map<String, Object> json = (Map<String, Object>) runAndParseJson(0, EXAMPLE_PATH.toAbsolutePath().toString());

        assertTrue(json.containsKey("jhove"));
        assertTrue(json.containsKey("exiftool"));
        assertTrue(((Map<String, Object>) json.get("exiftool")).containsKey("TIFF"));
    }

    /**
     * Check that a simple JsonPath can be evaluated for a tif image.
     *
     * @throws JsonMappingException in case json parsing fails
     * @throws JsonProcessingException in case json parsing fails
     */
    @SuppressWarnings("unchecked")
    @Test
    public void evaluateValidJsonPath() throws JsonMappingException, JsonProcessingException {
        List<String> json = (List<String>) runAndParseJson(0, "-p=$.exiftool..ImageWidth", EXAMPLE_PATH.toAbsolutePath().toString());

        assertEquals("[16]", json.toString());
    }

    /**
     * Check that a incorrect JsonPath causes non-zero exit code.
     *
     * @throws JsonMappingException in case json parsing fails
     * @throws JsonProcessingException in case json parsing fails
     */
    @Test
    public void evaluateInvalidJsonPath() throws JsonMappingException, JsonProcessingException {
        assertNull(runAndParseJson(1, "-p=$.something", EXAMPLE_PATH.toAbsolutePath().toString()));
    }

    private Object runAndParseJson(int expectedExitCode, String... args) throws JsonMappingException, JsonProcessingException {
        StringWriter stdout = new StringWriter();
        CommandLine cmd = new CommandLine(new ExtractCommand());

        // run
        cmd.setOut(new PrintWriter(stdout));
        int exitCode = cmd.execute(args);
        assertEquals(expectedExitCode, exitCode);

        // parse json output
        return new ObjectMapper().readValue(stdout.toString(), new TypeReference<Object>() {});
    }
}
