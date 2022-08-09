package co.axelrod.chatwords.dictionary.oxford;

import co.axelrod.chatwords.dictionary.oxford.list.LevelA1Words;
import co.axelrod.chatwords.dictionary.oxford.list.LevelA2Words;
import co.axelrod.chatwords.dictionary.oxford.list.LevelB1Words;
import co.axelrod.chatwords.dictionary.oxford.list.LevelB2Words;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@UtilityClass
public class OxfordWordGenerator {
    private static final Random RANDOM = new SecureRandom();

    public static List<String> getAllWords(Level level) {
        switch (level) {
            case A1: {
                return LevelA1Words.WORDS;
            }
            case A2: {
                return LevelA2Words.WORDS;
            }
            case B1: {
                return LevelB1Words.WORDS;
            }
            case B2: {
                return LevelB2Words.WORDS;
            }
            default:
                throw new ChatWordsRuntimeException("Unable to get all words for Oxford level: " + level);
        }
    }

    public static String getRandomWord(Level level) {
        switch (level) {
            case A1: {
                return LevelA1Words.WORDS.get(RANDOM.nextInt(LevelA1Words.WORDS.size()));
            }
            case A2: {
                return LevelA2Words.WORDS.get(RANDOM.nextInt(LevelA2Words.WORDS.size()));
            }
            case B1: {
                return LevelB1Words.WORDS.get(RANDOM.nextInt(LevelB1Words.WORDS.size()));
            }
            case B2: {
                return LevelB2Words.WORDS.get(RANDOM.nextInt(LevelB2Words.WORDS.size()));
            }
            default: {
                throw new ChatWordsRuntimeException("Unable to get random word for Oxford level: " + level);
            }
        }
    }
}
