package de.spkberlin.stabiv.cli.commands.options;

import de.spkberlin.stabiv.cli.common.Constants;
import picocli.CommandLine.Command;

@Command(
    mixinStandardHelpOptions = true, // automatically add help option
    usageHelpAutoWidth = true, // auto format help according to terminal width
    showDefaultValues = true, // append default values to option help texts
    version = {
        Constants.CLI_COMMAND_NAME + " v" + Constants.VERSION,
        "",
        "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
        "OS: ${os.name} ${os.version} ${os.arch}",
    }
)
public class CommonOptions {
    //
}
