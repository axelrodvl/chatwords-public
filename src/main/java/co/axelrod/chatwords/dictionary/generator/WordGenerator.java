package co.axelrod.chatwords.dictionary.generator;

import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.generator.lang.*;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@UtilityClass
public class WordGenerator {
    private static final Random RANDOM = new SecureRandom();

    public static List<String> getAllWords(Language language) {
        if (!UserContext.getUser().getDefaultFromLanguage().equals(Language.UKRAINIAN)) {
            switch (language) {
                case ENGLISH:
                    return EnglishWords.WORDS;
                case GERMAN:
                    return GermanWords.WORDS;
                case RUSSIAN:
                    return RussianWords.WORDS;
                case SPANISH:
                    return SpanishWords.WORDS;
                case FRENCH:
                    return FrenchWords.WORDS;
                case ITALIAN:
                    return ItalianWords.WORDS;
                case PORTUGUESE:
                    return PortugueseWords.WORDS;
                case TURKISH:
                    return TurkishWords.WORDS;
                case UKRAINIAN:
                    return UkrainianWords.WORDS;
                default:
                    throw new ChatWordsRuntimeException("Unable to get all words for language: " + language);
            }
        } else {
            List<String> englishWords = new ArrayList<>(EnglishWords.WORDS);
            englishWords.removeAll(Arrays.asList(
                    "internet",
                    "website",
                    "makeup",
                    "sibling",
                    "ipad",
                    "halloween",
                    "parenting",
                    "ethnicity",
                    "middle",
                    "genome",
                    "google",
                    "email",
                    "african",
                    "statewide",
                    "cellphone",
                    "grad",
                    "day",
                    "four",
                    "layoff",
                    "taliban",
                    "ex",
                    "maverick",
                    "preseason",
                    "momma",
                    "fm",
                    "nonfiction",
                    "outnumber",
                    "divisive",
                    "centerpiece",
                    "shitty",
                    "long",
                    "islamist",
                    "cherokee",
                    "bullpen",
                    "upbeat",
                    "middle-class",
                    "african-american",
                    "day-to-day",
                    "four-year",
                    "ex-wife",
                    "long-distance"
            ));
            englishWords.addAll(Arrays.asList(
                    "socialist",
                    "volatility",
                    "sly",
                    "biopsy",
                    "penetration",
                    "handmade",
                    "featured",
                    "waver",
                    "genesis",
                    "citrus",
                    "antitrust",
                    "apprehension",
                    "in",
                    "solidify",
                    "gut",
                    "scrub",
                    "aggravate",
                    "savage",
                    "oriental",
                    "staffing",
                    "entree",
                    "diarrhea",
                    "countdown",
                    "colonization",
                    "fume",
                    "skate",
                    "sarcasm",
                    "cynicism",
                    "exploit",
                    "caterpillar",
                    "aboriginal",
                    "bundle",
                    "relativity",
                    "safeguard",
                    "furnishings",
                    "holler"
            ));
            return englishWords;
        }
    }

    public static String getRandomWord(Language language) {
        switch (language) {
            case ENGLISH:
                return EnglishWords.WORDS.get(RANDOM.nextInt(EnglishWords.WORDS.size()));
            case GERMAN:
                return GermanWords.WORDS.get(RANDOM.nextInt(GermanWords.WORDS.size()));
            case RUSSIAN:
                return RussianWords.WORDS.get(RANDOM.nextInt(RussianWords.WORDS.size()));
            case SPANISH:
                return SpanishWords.WORDS.get(RANDOM.nextInt(SpanishWords.WORDS.size()));
            case FRENCH:
                return FrenchWords.WORDS.get(RANDOM.nextInt(FrenchWords.WORDS.size()));
            case ITALIAN:
                return ItalianWords.WORDS.get(RANDOM.nextInt(ItalianWords.WORDS.size()));
            case PORTUGUESE:
                return PortugueseWords.WORDS.get(RANDOM.nextInt(PortugueseWords.WORDS.size()));
            case TURKISH:
                return TurkishWords.WORDS.get(RANDOM.nextInt(TurkishWords.WORDS.size()));
            case UKRAINIAN:
                return UkrainianWords.WORDS.get(RANDOM.nextInt(UkrainianWords.WORDS.size()));
            default:
                throw new ChatWordsRuntimeException("Unable to get random word for language: " + language);
        }
    }
}
