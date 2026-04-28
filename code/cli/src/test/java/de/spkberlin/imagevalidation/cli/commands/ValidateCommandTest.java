package de.spkberlin.imagevalidation.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.spkberlin.imagevalidation.cli.common.TestConstants;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class ValidateCommandTest {

    private static final Path EXAMPLE_PATH = TestConstants.EXAMPLES_PATH.resolve("simple/wellformed/simple_rgb.tif");
    private static final Path UNKNOWN_PATH = TestConstants.EXAMPLES_PATH.resolve("unknown.tif");

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
        int exitCode = cmd.execute("-vvv", "--imageRule=$.jhove.wellformed/equal/true", EXAMPLE_PATH.toAbsolutePath().toString());
        assertEquals(0, exitCode);
    }
}
