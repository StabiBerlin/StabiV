package de.spkberlin.imagevalidation.cli.commands;

import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "extract", description = "%nExtract and list image properties for a single image file.")
public class ExtractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ExtractCommand.class);

    @Mixin
    private CommonOptions commonOptions;

    /** Wether to show additional log messages. */
    @Option(names = { "-v", "--verbose" }, description = "print more logging (use `-vv` for debug logging or `-vvv` for trace logging)")
    boolean[] verbosity;

    /** Which extraction modules to apply */
    @Option(
        names = { "--extractors" },
        split = ",",
        defaultValue = "exiftool,jhove",
        paramLabel = "module",
        description = "Extraction modules that are applied to each image file"
    )
    ExtractorType[] extractors = {};

    /** The path to the file whose properties are extracted. */
    @Parameters(paramLabel = "PATH", index = "0", description = "individual file to extract properties from")
    Path filepath;

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(false);
        Logging.setLogLevel(Objects.isNull(verbosity) ? 0 : verbosity.length);

        logger.info("run extract command");

        logger.info("extract properties from file '{}'", filepath);

        for (ExtractorType extratorType : extractors) {
            logger.debug("apply extractor {}", extratorType);

            if (extratorType.equals(ExtractorType.EXIFTOOL)) {
                Extractor extractor = new ExiftoolExtractor();
                ExtractionResult extractionResult = extractor.extractProperties(filepath);

                for (Entry<String, String> entry : extractionResult.properties().entrySet()) {
                    System.out.println(entry.getKey());
                    System.out.println("    " + entry.getValue());
                }
            }
        }

        logger.info("done extracting properties");

        return 0;
    }
}
