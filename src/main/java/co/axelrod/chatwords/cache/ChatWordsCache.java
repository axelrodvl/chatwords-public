package co.axelrod.chatwords.cache;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ChatWordsCache {
    public static final String USERS = "users";
    public static final String TRANSLATIONS = "translations";
    public static final String DETERMINE_LANGUAGE = "determineLanguage";
    public static final String POSSIBLE_LANGUAGE = "possibleLanguage";
    public static final String WORDS = "words";

    public static List<String> getCachesToEvict() {
        return Arrays.asList(
                USERS,
                TRANSLATIONS,
                DETERMINE_LANGUAGE,
                POSSIBLE_LANGUAGE
        );
    }
}
