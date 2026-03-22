package de.spkberlin.imagevalidation.cli.commands;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.cli.commands.options.CommonOptions;
import de.spkberlin.imagevalidation.cli.common.Logging;
import de.spkberlin.imagevalidation.cli.extraction.ExtractorType;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import de.spkberlin.imagevalidation.common.properties.PropertiesSerializer;
import de.spkberlin.imagevalidation.exiftool.ExiftoolExtractor;
import de.spkberlin.imagevalidation.jhove.JHoveExtractor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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

    Extract a subsection of properties for a file using the JHove module:%n
      ${COMMAND-FULL-NAME} --extractor=jhove --property="$..NisoImageMetadata" /path/to/example.tif
    """
)
public class ExtractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ExtractCommand.class);

    @Mixin
    private CommonOptions commonOptions;

    @ArgGroup(validate = true, heading = "%n@|bold Output Options|@%n%n", order = 3, exclusive = false)
    private OutputOptions outputOptions = new OutputOptions();

    static class OutputOptions {

        /** wether to show additional log messages. */
        @Option(names = { "-v", "--verbose" }, description = "print more logging (use `-vv` for debug logging or `-vvv` for trace logging)")
        boolean[] verbosity;
    }

    @ArgGroup(validate = true, heading = "%n@|bold Extraction Options|@%n%n", order = 2, exclusive = false)
    public ExtractionOptions extractionOptions = new ExtractionOptions();

    static class ExtractionOptions {

        /** The extraction modules that will be applied. */
        @Option(
            names = { "--extractor" },
            paramLabel = "MODULE",
            description = """
            Extraction module that is applied to each image file.

            Possible values:
              - `jhove` - extract image properties via JHove, see https://jhove.openpreservation.org/
              - `exiftool` - extract image properties via Exiftool, see https://exiftool.org/
            """
        )
        List<ExtractorType> extractors = List.of(ExtractorType.exiftool, ExtractorType.jhove);

        /** The property path to resolve via JsonPath. */
        @Option(
            names = { "-p", "--property" },
            paramLabel = "PROPERTY",
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

    @ArgGroup(validate = true, heading = "%n@|bold Input Options|@%n%n", order = 1, exclusive = false, multiplicity = "1")
    public InputOptions inputOptions = new InputOptions();

    static class InputOptions {

        /** The path to the file whose properties are extracted. */
        @Parameters(
            paramLabel = "PATH",
            index = "0",
            arity = "1",
            description = "filepath to individual file whose properties are extracted"
        )
        Path filepath;
    }

    /** Called when CLI arguments were successfully parsed. */
    @Override
    public Integer call() throws Exception {
        Logging.setLogMode(false);
        Logging.setLogLevel(Objects.isNull(outputOptions.verbosity) ? 0 : outputOptions.verbosity.length);

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
            try {
                outputProperties = JsonPath.read(properties, extractionOptions.jsonPath);
            } catch (PathNotFoundException e) {
                logger.error("path '%extractionOptions.jsonPath' not found in extracted properties");
                outputProperties = null;
            }
        }

        System.out.println(PropertiesSerializer.serializeAsPrettyJson(outputProperties));

        logger.info("done extracting properties");

        return 0;
    }
}
