package org.harze.yuyukobot;

import org.apache.commons.validator.routines.UrlValidator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    private static final String[] schemes = {"http","https"};

    public static boolean validUrl(String url) {
        if (url == null || url.isBlank())
            return false;
        return new UrlValidator(schemes).isValid(url);
    }

    public static Timestamp now() {
        return new Timestamp(Instant.now().toEpochMilli());
    }

    public static String tsQueryBuilder(List<List<String>> tagGroups) {
        return tagGroups.stream()
                .map(tagGroup -> tagGroup.stream()
                        .map(tag -> tag.contains(" ") ? "''" + tag + "''" : tag) // Escaped simple quotes if the tag contains spaces
                        .collect(Collectors.joining(" & "))) // AND operator for tsquery
                .map(clause -> "(" + clause +")") // Surround AND clauses in parenthesis
                .collect(Collectors.joining(" | ")); // Finally OR of the clauses
    }
}
