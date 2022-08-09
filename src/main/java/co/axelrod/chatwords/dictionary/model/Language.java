package co.axelrod.chatwords.dictionary.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    RUSSIAN("ru", "russian", "\uD83C\uDDF7\uD83C\uDDFA", true, true),
    ENGLISH("en", "english", "\uD83C\uDDFA\uD83C\uDDF8", false, true),
    GERMAN("de", "german", "\uD83C\uDDE9\uD83C\uDDEA", false, false),
    SPANISH("es", "spanish", "\uD83C\uDDEA\uD83C\uDDF8", false, false),
    FRENCH("fr", "french", "\uD83C\uDDEB\uD83C\uDDF7", false, false),
    ITALIAN("it", "italian", "\uD83C\uDDEE\uD83C\uDDF9", false, false),
    PORTUGUESE("pt", "portuguese", "\uD83C\uDDF5\uD83C\uDDF9", false, false),
    TURKISH("tr", "turkish", "\uD83C\uDDF9\uD83C\uDDF7", false, false),
    UKRAINIAN("uk", "ukrainian", "\uD83C\uDDFA\uD83C\uDDE6", true, false);

    private final String value;
    private final String fullName;
    private final String emoji;
    private final boolean userInterface;
    private final boolean ukrainianSupport;

    public static Language getLanguageById(String id) {
        for (Language language : values()) {
            if (language.value.equals(id)) {
                return language;
            }
        }
        // TODO Китайский и все прочие языки
        return null;
    }
}
