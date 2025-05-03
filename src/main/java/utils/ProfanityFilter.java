package utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfanityFilter {

    private static final Set<String> badWords = Set.of(
            "idiot", "stupid", "insulte", "merde", "con", "fool"
    );

    public static boolean containsBadWord(String input) {
        return !extractBadWords(input).isEmpty();
    }

    public static List<String> extractBadWords(String input) {
        if (input == null) return List.of();
        return Arrays.stream(input.toLowerCase().split("\\s+"))
                .filter(badWords::contains)
                .collect(Collectors.toList());
    }
}
