package de.spkberlin.stabiv.common.validation.directories;

import de.spkberlin.stabiv.common.math.NumberConsecutiveness;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsecutiveNumberingCondition;
import de.spkberlin.stabiv.common.validation.directories.state.DirectoryConsecutiveNumberingResult;
import de.spkberlin.stabiv.common.validation.state.ValidationFailureSeverity;
import de.spkberlin.stabiv.common.validation.state.ValidationResult;
import java.nio.file.Path;
import java.util.IllegalFormatException;
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
                ValidationResult.fromSuccess(),
                null
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
        Map<Path, ValidationResult> imageResults = compileImageResults(numbersByImage, duplicateNumbers, condition.getSeverity());

        // check for gaps
        List<String> messages = NumberConsecutiveness.findGaps(numbersList, condition.getStartNumber())
            .stream()
            .map(n -> {
                try {
                    if (Objects.nonNull(condition.getFilenameTemplate()) && !condition.getFilenameTemplate().isBlank()) {
                        return String.format(
                            "image '%s' is missing, which violates consecutiveness",
                            String.format(condition.getFilenameTemplate(), n)
                        );
                    }
                } catch (IllegalFormatException e) {
                    logger.warn("filename template '{}' does not accept single number (%d parameter)");
                }
                return String.format("number %d is missing, which violates consecutiveness", n);
            })
            .toList();

        return new DirectoryConsecutiveNumberingResult(
            imageResults,
            ValidationResult.of(messages, condition.getSeverity()),
            numbersList.stream().max(Integer::compareTo).orElse(null)
        );
    }

    private static Map<Path, Optional<Integer>> matchFilenames(List<Path> filepaths, Pattern pattern, Integer matchGroup) {
        return filepaths
            .stream()
            .collect(
                Collectors.toMap(
                    filepath -> filepath,
                    filepath -> {
                        try {
                            Matcher m = pattern.matcher(Objects.toString(filepath.getFileName(), ""));
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

    private static Map<Path, ValidationResult> compileImageResults(
        Map<Path, Optional<Integer>> extractedNumbers,
        Set<Integer> duplicates,
        ValidationFailureSeverity severity
    ) {
        return extractedNumbers
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (!entry.getValue().isPresent()) {
                        return ValidationResult.of("could not extract number from filename", severity);
                    } else if (duplicates.contains(entry.getValue().get())) {
                        return ValidationResult.of("extracted number occurrs more than once for consecutiveness check", severity);
                    }
                    return ValidationResult.fromSuccess();
                })
            );
    }
}
