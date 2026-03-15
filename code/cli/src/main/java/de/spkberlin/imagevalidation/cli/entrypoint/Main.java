package de.spkberlin.imagevalidation.cli.entrypoint;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_AT_FILE_PARAMETER;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

import de.spkberlin.imagevalidation.cli.commands.ValidateCommand;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine;

/** Main class of CLI, which triggers parsing of arguments. */
public class Main {

    public static void main(String... args) {
        // set up validate command
        CommandLine cmd = new CommandLine(new ValidateCommand());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);

        // move "at file" help hint to options section
        List<String> copy = new ArrayList<>(cmd.getHelpSectionKeys());
        copy.remove(SECTION_KEY_AT_FILE_PARAMETER);
        copy.add(copy.indexOf(SECTION_KEY_OPTION_LIST), SECTION_KEY_AT_FILE_PARAMETER);
        cmd.setHelpSectionKeys(copy);

        // execute validate command
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
