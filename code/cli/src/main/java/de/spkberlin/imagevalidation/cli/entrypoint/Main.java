package de.spkberlin.imagevalidation.cli.entrypoint;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_AT_FILE_PARAMETER;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_DESCRIPTION;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

import de.spkberlin.imagevalidation.cli.commands.MainCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.IHelpSectionRenderer;

/** Main class of CLI, which triggers parsing of arguments. */
public class Main {

    public static void main(String... args) {
        // set up validate command
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);

        /*cmd.setHelpFactory(commandSpec -> {
            CommandLine.Help help = new CommandLine.Help(commandSpec) {
                @Override
                public String toString() {
                    String result = super.toString();
                    return result.replace("&ast;", "*");
                }
            };
            return help;
        });*/

        Map<String, IHelpSectionRenderer> helpSectionMap = new HashMap<>(cmd.getHelpSectionMap());
        helpSectionMap.put(SECTION_KEY_DESCRIPTION, help -> help.optionList().replace("&ast;", "*"));
        helpSectionMap.put(SECTION_KEY_OPTION_LIST, help -> help.optionList().replace("&ast;", "*"));
        cmd.setHelpSectionMap(helpSectionMap);

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
