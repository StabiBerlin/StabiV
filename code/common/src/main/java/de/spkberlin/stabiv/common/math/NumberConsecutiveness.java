package de.spkberlin.stabiv.common.math;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NumberConsecutiveness {

    /**
     * Checks a list of numbers for consecutiveness given a starting number, assuming an increment of 1.
     *
     * @param numberList the list of numbers
     * @param startNumber the starting number
     * @return an error message or null, in case of success
     */
    public static List<Integer> findGaps(List<Integer> numberList, Integer startNumber) throws IllegalArgumentException {
        if (numberList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> numberSet = new HashSet<>(numberList);
        return IntStream.range(startNumber, Collections.max(numberSet) + 1)
            .boxed()
            .filter(i -> !numberSet.contains(i))
            .toList();
    }

    /**
     * Given a list of integers, find all duplicates.
     *
     * @param numberList the list of integers
     * @return the set of integers that occur multiple times
     */
    public static Set<Integer> findDuplicates(List<Integer> numberList) {
        Set<Integer> seenSet = new HashSet<>();
        return numberList
            .stream()
            .filter(i -> !seenSet.add(i))
            .collect(Collectors.toSet());
    }
}
