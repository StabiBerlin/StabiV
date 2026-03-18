package de.spkberlin.imagevalidation.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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

        Map<String, Object> propertyTree = new HashMap<>();
        for (ExtractorType extratorType : extractors) {
            logger.debug("apply extractor {}", extratorType);

            if (extratorType.equals(ExtractorType.exiftool)) {
                Extractor extractor = new ExiftoolExtractor();
                propertyTree.put(ExtractorType.exiftool.name(), extractor.extractProperties(filepath));
            }

            if (extratorType.equals(ExtractorType.jhove)) {
                Extractor extractor = new JHoveExtractor();
                propertyTree.put(ExtractorType.jhove.name(), extractor.extractProperties(filepath));
            }
        }

        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(propertyTree);
        System.out.println(json);

        logger.info("done extracting properties");

        return 0;
    }
}
