package de.spkberlin.imagevalidation.common.validation.directories;

import de.spkberlin.imagevalidation.common.math.NumberConsecutiveness;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsecutiveNumberingCondition;
import de.spkberlin.imagevalidation.common.validation.directories.state.DirectoryConsecutiveNumberingResult;
import de.spkberlin.imagevalidation.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryConsecutivenessValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryConsecutivenessValidation.class);

    /**
     * Checks whether a set of filepaths follows a consecutive numbering pattern.
     *
     * @param condition the conseuctive numbering condition to check
     * @param imageFilepaths a set of image filepaths to verify for consecutiveness
     * @return an error message if files are not correctly consecutive, null if success
     */
    public static DirectoryConsecutiveNumberingResult evaluateConsecutiveness(
        DirectoryConsecutiveNumberingCondition condition,
        List<Path> imageFilepaths
    ) {
        if (Objects.isNull(condition.getFilenameRegExp()) || condition.getFilenameRegExp().isEmpty()) {
            logger.debug("skip consecutiveness check due to empty filenameRegExp");
            return new DirectoryConsecutiveNumberingResult(
                imageFilepaths.stream().collect(Collectors.toMap(filepath -> filepath, filepath -> ValidationResult.fromSuccess())),
                ValidationResult.fromSuccess()
            );
        }

        logger.debug("evaluate consecutive numbering with pattern '{}'", condition.getFilenameRegExp());
        // extract numbers
        Map<Path, Optional<Integer>> numbersByImage = matchFilenames(
            imageFilepaths,
            Pattern.compile(condition.getFilenameRegExp()),
            condition.getNumberMatchGroup()
        );

        // check for duplicates
        List<Integer> numbersList = numbersByImage.values().stream().filter(Optional::isPresent).map(Optional::get).toList();
        Set<Integer> duplicateNumbers = NumberConsecutiveness.findDuplicates(numbersList);

        // generate error messages for individual images
        Map<Path, ValidationResult> imageResults = compileImageResults(numbersByImage, duplicateNumbers);

        // check for gaps
        ValidationResult gapResult = ValidationResult.of(
            NumberConsecutiveness.findGaps(numbersList, condition.getStartNumber())
                .stream()
                .map(n -> String.format("number %d is missing, which violates consecutiveness", n))
                .toList(),
            Collections.emptyList()
        );

        // combine into final result object
        // ValidationResult imageSummaryResult = ValidationResult.ofFirst(imageResults.values());
        // ValidationResult directoryResult = ValidationResult.of(Stream.of(imageSummaryResult, gapResult).toList());
        return new DirectoryConsecutiveNumberingResult(imageResults, gapResult);
    }

    private static Map<Path, Optional<Integer>> matchFilenames(List<Path> filepaths, Pattern pattern, Integer matchGroup) {
        return filepaths
            .stream()
            .collect(
                Collectors.toMap(
                    filepath -> filepath,
                    filepath -> {
                        try {
                            Matcher m = pattern.matcher(filepath.getFileName().toString());
                            Boolean found = m.find();
                            String group = m.group(matchGroup);
                            Integer number = Integer.parseInt(group);
                            if (!found || Objects.isNull(group) || Objects.isNull(number)) {
                                return Optional.empty();
                            }
                            return Optional.of(number);
                        } catch (IllegalStateException e) {
                            return Optional.empty();
                        } catch (IndexOutOfBoundsException e) {
                            return Optional.empty();
                        } catch (NumberFormatException e) {
                            return Optional.empty();
                        }
                    }
                )
            );
    }

    private static Map<Path, ValidationResult> compileImageResults(Map<Path, Optional<Integer>> extractedNumbers, Set<Integer> duplicates) {
        return extractedNumbers
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (!entry.getValue().isPresent()) {
                        return ValidationResult.fromError("could not extract number from filename");
                    } else if (duplicates.contains(entry.getValue().get())) {
                        return ValidationResult.fromError("extracted number occurrs more than once for consecutiveness check");
                    }
                    return ValidationResult.fromSuccess();
                })
            );
    }
}
