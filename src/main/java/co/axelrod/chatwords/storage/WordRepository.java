package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.cache.ChatWordsCache;
import co.axelrod.chatwords.dictionary.model.Language;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends MongoRepository<Word, String> {
    List<Word> findWordByRussian(String russian);

    List<Word> findWordByEnglish(String english);

    List<Word> findWordByGerman(String german);

    List<Word> findWordBySpanish(String spanish);

    List<Word> findWordByFrench(String french);

    List<Word> findWordByItalian(String italian);

    List<Word> findWordByPortuguese(String portuguese);

    List<Word> findWordByTurkish(String turkish);

    List<Word> findWordByUkrainian(String ukrainian);

    @Cacheable(value = ChatWordsCache.WORDS, unless = "#result == null")
    default Optional<Word> findWord(String word, Language language) {
        switch (language) {
            case RUSSIAN:
                return findWordByRussian(word).stream().findFirst();
            case ENGLISH:
                return findWordByEnglish(word).stream().findFirst();
            case GERMAN:
                return findWordByGerman(word).stream().findFirst();
            case SPANISH:
                return findWordBySpanish(word).stream().findFirst();
            case FRENCH:
                return findWordByFrench(word).stream().findFirst();
            case ITALIAN:
                return findWordByItalian(word).stream().findFirst();
            case PORTUGUESE:
                return findWordByPortuguese(word).stream().findFirst();
            case TURKISH:
                return findWordByTurkish(word).stream().findFirst();
            case UKRAINIAN:
                return findWordByUkrainian(word).stream().findFirst();
            default:
                return Optional.empty();
        }
    }

    default List<Word> findWords(String word, Language language) {
        switch (language) {
            case RUSSIAN:
                return findWordByRussian(word);
            case ENGLISH:
                return findWordByEnglish(word);
            case GERMAN:
                return findWordByGerman(word);
            case SPANISH:
                return findWordBySpanish(word);
            case FRENCH:
                return findWordByFrench(word);
            case ITALIAN:
                return findWordByItalian(word);
            case PORTUGUESE:
                return findWordByPortuguese(word);
            case TURKISH:
                return findWordByTurkish(word);
            case UKRAINIAN:
                return findWordByUkrainian(word);
            default:
                return Collections.emptyList();
        }
    }
}
