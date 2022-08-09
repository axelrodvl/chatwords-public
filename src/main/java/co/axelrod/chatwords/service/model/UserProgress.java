package co.axelrod.chatwords.service.model;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.UserDictionary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProgress extends UserDto {
    private final List<Language> languages;

    private final Integer wordsCount;
    private final Integer learnedWordsCount;
    private final Float totalScore;

    private final UserDictionary currentDictionary;
}
