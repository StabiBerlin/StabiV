package de.spkberlin.imagevalidation.cli.entrypoint;

import de.spkberlin.imagevalidation.cli.commands.ValidateCommand;
import picocli.CommandLine;

/** Main class of CLI, which triggers parsing of arguments. */
public class Main {

    public static void main(String... args) {
        int exitCode = new CommandLine(new ValidateCommand()).execute(args);
        System.exit(exitCode);
    }
}
