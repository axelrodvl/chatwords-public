package co.axelrod.chatwords.dictionary.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageByCountry {
    RUSSIAN("ru", "\uD83C\uDDF7\uD83C\uDDFA"),
    ENGLISH("en", "\uD83C\uDDFA\uD83C\uDDF8"),
    GERMAN("de", "\uD83C\uDDE9\uD83C\uDDEA"),
    SPANISH("es", "\uD83C\uDDEA\uD83C\uDDF8"),
    FRENCH("fr", "\uD83C\uDDEB\uD83C\uDDF7"),
    ITALIAN("it", "\uD83C\uDDEE\uD83C\uDDF9"),
    PORTUGUESE("pt", "\uD83C\uDDF5\uD83C\uDDF9"),
    TURKISH("tr", "\uD83C\uDDF9\uD83C\uDDF7"),
    UKRAINIAN("uk", "\uD83C\uDDFA\uD83C\uDDE6"),

    // TODO Добавить сюда страны и перевод
    CHINESE("", "\uD83C\uDDE8\uD83C\uDDF3"),
    HINDI("", "\uD83C\uDDEE\uD83C\uDDF3");

    private final String value;
    private final String emoji;

    public static LanguageByCountry getLanguageById(String id) {
        for (LanguageByCountry language : values()) {
            if (language.value.equals(id)) {
                return language;
            }
        }
        return null;
    }
}
