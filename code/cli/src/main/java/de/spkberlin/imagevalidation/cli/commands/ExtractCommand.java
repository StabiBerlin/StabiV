package de.spkberlin.imagevalidation.cli.commands;

import com.jayway.jsonpath.JsonPath;
import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.extractors.ExtractorType;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "extract",
    optionListHeading = "@|bold General Options|@%n%n",
    description = """
    %nExtract and list image properties for a single image file.

    @|bold Examples|@

    Extract all properties of a file:%n
      ${COMMAND-FULL-NAME} /path/to/example.tif

    Extract a specific property for a file:%n
      ${COMMAND-FULL-NAME} --property="$..PhotometricInterpretation" /path/to/example.tif

    Extract a subsection of properties for a file:%n
      ${COMMAND-FULL-NAME} --extractors=jhove --property="$..NisoImageMetadata" /path/to/example.tif
    """
)
public class ExtractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ExtractCommand.class);

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Logging Options|@%n%n", order = 3, exclusive = false)
    private LoggingOptions loggingOptions = new LoggingOptions();

    static class LoggingOptions {

        /** wether to show additional log messages. */
        @Option(names = { "-v", "--verbose" }, description = "print more logging (use `-vv` for debug logging or `-vvv` for trace logging)")
        boolean[] verbosity;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Extraction Options|@%n%n", order = 2, exclusive = false)
    public ExtractionOptions extractionOptions = new ExtractionOptions();

    static class ExtractionOptions {

        /** which extraction modules to apply */
        @Option(
            names = { "--extractors" },
            split = ",",
            defaultValue = "exiftool,jhove",
            paramLabel = "module",
            description = "Extraction modules that are applied to each image file"
        )
        ExtractorType[] extractors = {};

        /** the property to resolve via JsonPath */
        @Option(
            names = { "-p", "--property" },
            paramLabel = "JSON_PATH",
            description = """
            An optional property selector (JsonPath) to limit what property or properties are printed

            Examples
            - `$` - all properties
            - `$.jhove` - only properties extracted by JHove
            - `$.jhove..IFD[0]` - only properties of first IFD extracted by JHove

            See RFC 9535 (https://datatracker.ietf.org/doc/html/rfc9535)
            """
        )
        String jsonPath;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Input Options|@%n%n", order = 1, exclusive = false)
    public InputOptions inputOptions = new InputOptions();

    static class InputOptions {

        /** The path to the file whose properties are extracted. */
        @Parameters(paramLabel = "PATH", index = "0", description = "filepath to individual file whose properties are extracted")
        Path filepath;
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(false);
        Logging.setLogLevel(Objects.isNull(loggingOptions.verbosity) ? 0 : loggingOptions.verbosity.length);

        logger.info("run extract command");

        logger.info("extract properties from file '{}'", inputOptions.filepath);

        Map<ExtractorType, Extractor> extractorsMap = Map.of(
            ExtractorType.exiftool,
            new ExiftoolExtractor(),
            ExtractorType.jhove,
            new JHoveExtractor()
        );

        Map<String, Object> properties = new HashMap<>();
        for (ExtractorType extratorType : extractionOptions.extractors) {
            logger.debug("apply extractor {}", extratorType);
            properties.put(extratorType.name(), extractorsMap.get(extratorType).extractProperties(inputOptions.filepath));
        }

        Object outputProperties = properties;
        if (Objects.nonNull(extractionOptions.jsonPath) && !extractionOptions.jsonPath.isEmpty()) {
            logger.debug("resolve json path '{}'", extractionOptions.jsonPath);
            outputProperties = JsonPath.read(properties, extractionOptions.jsonPath);
        }

        System.out.println(PropertiesSerializer.serializeProperties(outputProperties));

        logger.info("done extracting properties");

        return 0;
    }
}
