package de.spkberlin.imagevalidation.cli.validation;

import de.spkberlin.imagevalidation.cli.configuration.state.ConfigurationValidationConsecutiveNumbering;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryConsecutivenessValidation {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryConsecutivenessValidation.class);

    /**
     * Checks whether a set of filepaths follows a consecutive numbering pattern.
     *
     * @param config consecutive numbering configuration
     * @param imageFilepaths a set of image filepaths to verify for consecutiveness
     * @return an error message if files are not correctly consecutive, null if success
     */
    public static String evaluateConsecutiveness(ConfigurationValidationConsecutiveNumbering config, Set<Path> imageFilepaths) {
        if (Objects.isNull(config.getFilenameRegExp()) || config.getFilenameRegExp().isEmpty()) {
            logger.debug("skip consecutiveness check due to empty filenameRegExp");
            return null;
        }

        logger.debug("evaluate consecutive numbering with pattern '{}'", config.getFilenameRegExp());
        Pattern pattern = Pattern.compile(config.getFilenameRegExp());

        List<String> filenames = imageFilepaths
            .stream()
            .map(p -> p.getFileName().toString())
            .toList();

        String unmatchedFilename = findFirstUnmatchedFilename(filenames, pattern, config.getNumberMatchGroup());
        if (Objects.nonNull(unmatchedFilename)) {
            return String.format(
                "filename '%s' doesn't match regular expression '%s' for consecutiveness check",
                unmatchedFilename,
                config.getFilenameRegExp()
            );
        }

        List<Integer> numberList = getNumbersFromFilenames(filenames, pattern, config.getNumberMatchGroup());
        String consecutivenessError = checkNumbersForConsecutiveness(numberList, config.getStartNumber());
        if (Objects.nonNull(consecutivenessError)) {
            return consecutivenessError;
        }

        // success, all files are numbered in consecutive order
        return null;
    }

    private static String findFirstUnmatchedFilename(List<String> filenames, Pattern pattern, Integer matchGroup) {
        return filenames
            .stream()
            .filter(s -> {
                try {
                    Matcher m = pattern.matcher(s);
                    Boolean found = m.find();
                    String group = m.group(matchGroup);
                    Integer number = Integer.parseInt(group);
                    return !found || Objects.isNull(group) || Objects.isNull(number);
                } catch (IllegalStateException e) {
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    return true;
                } catch (NumberFormatException e) {
                    return true;
                }
            })
            .findFirst()
            .orElse(null);
    }

    private static List<Integer> getNumbersFromFilenames(List<String> filenames, Pattern pattern, Integer matchGroup) {
        return filenames
            .stream()
            .map(pattern::matcher)
            .map(m -> {
                m.find();
                return m.group(matchGroup);
            })
            .map(Integer::parseInt)
            .toList();
    }

    private static String checkNumbersForConsecutiveness(List<Integer> numberList, Integer startNumber) {
        Set<Integer> seenSet = new HashSet<>();
        Integer duplicate = numberList
            .stream()
            .filter(i -> !seenSet.add(i))
            .findFirst()
            .orElse(null);

        if (Objects.nonNull(duplicate)) {
            return String.format("found duplicate image number '%d' in the list of filenames", duplicate);
        }

        Integer numberListLength = numberList.size();
        Integer missing = IntStream.range(startNumber, numberListLength + startNumber)
            .boxed()
            .filter(i -> !seenSet.contains(i))
            .findFirst()
            .orElse(null);

        if (Objects.nonNull(missing)) {
            return String.format("could not find image number '%d' in the list of filenames", missing);
        }

        return null;
    }
}
