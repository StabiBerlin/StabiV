package de.spkberlin.stabiv.cli.commands;

import de.spkberlin.stabiv.cli.commands.options.CommonOptions;
import de.spkberlin.stabiv.cli.common.Constants;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = Constants.CLI_COMMAND_NAME,
    subcommands = { ValidateCommand.class, ExtractCommand.class },
    description = "stabiv command line interface"
)
public class MainCommand implements Callable<Integer> {

    @Mixin
    private CommonOptions commonOptions;

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 1;
    }
}
