package de.spkberlin.imagevalidation.common.validation.directories;

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
     * @param fileNameRegExp the regula expression that allows to extract a number from the filename
     * @param matchGroup the match group id of the match group that matches the number part of the filename
     * @param startNumber the starting number when counting files (e.g. 1, 2, 3, ... vs. 0, 1, 2, ...)
     * @param imageFilepaths a set of image filepaths to verify for consecutiveness
     * @return an error message if files are not correctly consecutive, null if success
     */
    public static String evaluateConsecutiveness(String fileNameRegExp, Integer matchGroup, Integer startNumber, Set<Path> imageFilepaths) {
        if (Objects.isNull(fileNameRegExp) || fileNameRegExp.isEmpty()) {
            logger.debug("skip consecutiveness check due to empty filenameRegExp");
            return null;
        }

        logger.debug("evaluate consecutive numbering with pattern '{}'", fileNameRegExp);
        Pattern pattern = Pattern.compile(fileNameRegExp);

        List<String> filenames = imageFilepaths
            .stream()
            .map(p -> p.getFileName().toString())
            .toList();

        String unmatchedFilename = findFirstUnmatchedFilename(filenames, pattern, matchGroup);
        if (Objects.nonNull(unmatchedFilename)) {
            return String.format(
                "filename '%s' doesn't match regular expression '%s' for consecutiveness check",
                unmatchedFilename,
                fileNameRegExp
            );
        }

        List<Integer> numberList = getNumbersFromFilenames(filenames, pattern, matchGroup);
        String consecutivenessError = checkNumbersForConsecutiveness(numberList, startNumber);
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

    /**
     * Checks a list of numbers for consecutiveness given a starting number, assuming an increment of 1.
     * @param numberList the list of numbers
     * @param startNumber the starting number
     * @return an error message or null, in case of success
     */
    public static String checkNumbersForConsecutiveness(List<Integer> numberList, Integer startNumber) {
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
