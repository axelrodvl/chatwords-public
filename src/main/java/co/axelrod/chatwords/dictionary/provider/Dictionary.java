package co.axelrod.chatwords.dictionary.provider;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.Word;

import java.util.Optional;

public interface Dictionary {
    Optional<Word> translate(String word, String languageCode, Language source, Language target);
}
