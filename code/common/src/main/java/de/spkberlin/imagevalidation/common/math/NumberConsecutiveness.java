package de.spkberlin.imagevalidation.common.math;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class NumberConsecutiveness {

    /**
     * Checks a list of numbers for consecutiveness given a starting number, assuming an increment of 1.
     *
     * @param numberList the list of numbers
     * @param startNumber the starting number
     * @return an error message or null, in case of success
     */
    public static String check(List<Integer> numberList, Integer startNumber) throws IllegalArgumentException {
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
            return String.format("image with number %d is missing, which violates consecutiveness", missing);
        }

        return null;
    }
}
