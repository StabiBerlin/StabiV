package de.spkberlin.imagevalidation.common.validation.directories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsistencyCondition;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryConsistencyValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryConsistencyValidation.class);

    /**
     * Evaluate whether properties have consistent values for each image in a directory.
     *
     * @param consistencyConditions the properties to check for consistency
     * @param extractionResults the extracted properties of all files of a directory
     * @return an error message in case properties are inconsistent, otherwise null
     * @throws IOException in case an error occurrs during validation
     */
    public static String evaluateAllPropertiesAreConsistent(
        List<DirectoryConsistencyCondition> consistencyConditions,
        Map<Path, ExtractionResult> extractionResults
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (DirectoryConsistencyCondition consistencyCondition : consistencyConditions) {
            Set<String> allValues = new HashSet<>();
            for (Entry<Path, ExtractionResult> entry : extractionResults.entrySet()) {
                Path filepath = entry.getKey();
                ExtractionResult result = entry.getValue();
                if (Objects.isNull(result)) {
                    return String.format(
                        "consistent property '%s' could not be extracted for image '%s'",
                        consistencyCondition.getLabel(),
                        filepath
                    );
                }
                try {
                    Object extractedProperty = JsonPath.read(result.getProperties(), consistencyCondition.getProperty());
                    allValues.add(objectMapper.writeValueAsString(extractedProperty));
                } catch (PathNotFoundException e) {
                    logger.trace("path '{}' not found in extracted properties", consistencyCondition.getProperty());
                    return String.format("consistent property '%s' not found in image '%s'", consistencyCondition.getLabel(), filepath);
                }
            }

            if (allValues.size() > 1) {
                return String.format(
                    "property '%s' is not consistent (has %d different values)",
                    consistencyCondition.getLabel(),
                    allValues.size()
                );
            }
        }

        return null;
    }
}
