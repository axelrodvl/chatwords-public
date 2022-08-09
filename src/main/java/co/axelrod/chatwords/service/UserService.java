package co.axelrod.chatwords.service;

import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.generator.WordGenerator;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.oxford.Level;
import co.axelrod.chatwords.dictionary.oxford.OxfordWordGenerator;
import co.axelrod.chatwords.dictionary.provider.Dictionary;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionary;
import co.axelrod.chatwords.service.model.ServiceResponse;
import co.axelrod.chatwords.service.model.UserProgress;
import co.axelrod.chatwords.storage.UserDictionary;
import co.axelrod.chatwords.storage.UserSettings;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.Word;
import co.axelrod.chatwords.storage.sorting.Sort;
import co.axelrod.chatwords.validator.DictionaryNameValidator;
import co.axelrod.chatwords.validator.model.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserService {
    public static final String THOUSAND_DICTIONARY_NAME = "1000 слов";

    private final DictionaryNameValidator dictionaryNameValidator;

    private final YandexDictionary yandexDictionary;
    private final Dictionary dictionary;

    public void setFromLanguage(Language from) {
        UserContext.getUser().setDefaultFromLanguage(from);
    }

    public void setToLanguage(Language to) {
        UserContext.getUser().setDefaultToLanguage(to);
    }

    public UserDictionary get1000WordsDictionary() {
        return UserContext.getUser().getDictionaries().get(THOUSAND_DICTIONARY_NAME);
    }

    public void create1000WordsDictionary(Language targetLanguage) {
        UserDictionary userDictionary = new UserDictionary(THOUSAND_DICTIONARY_NAME, UserContext.getUser().getDefaultFromLanguage(), targetLanguage);
        List<String> allWords = WordGenerator.getAllWords(targetLanguage);
        allWords.forEach(word -> {
            // Переводим и добавляем слово в словарь
            Optional<Word> translatedWord = yandexDictionary.translate(word, UserContext.getUser().getLanguageCode(), userDictionary.getTargetLanguage(), userDictionary.getSourceLanguage());
            translatedWord.ifPresent(w -> userDictionary.getWords().add(new UserWord(translatedWord.get())));
        });

        UserContext.getUser().getDictionaries().put(THOUSAND_DICTIONARY_NAME, userDictionary);
        UserContext.getUser().setCurrentDictionary(userDictionary);
    }

    public void createOxford3000Dictionary(Level level) {
        UserDictionary userDictionary = new UserDictionary(Level.getDictionaryName(level), UserContext.getUser().getDefaultFromLanguage(), Language.ENGLISH);
        List<String> allWords = OxfordWordGenerator.getAllWords(level);
        allWords.forEach(word -> {
            // Переводим и добавляем слово в словарь
            Optional<Word> translatedWord = yandexDictionary.translate(word, UserContext.getUser().getLanguageCode(), userDictionary.getTargetLanguage(), userDictionary.getSourceLanguage());
            translatedWord.ifPresent(w -> userDictionary.getWords().add(new UserWord(translatedWord.get())));
        });

        UserContext.getUser().getDictionaries().put(Level.getDictionaryName(level), userDictionary);
        UserContext.getUser().setCurrentDictionary(userDictionary);
    }

    public ServiceResponse createDictionary(String dictionaryName, Language source, Language target) {
        ValidationResult validationResult = dictionaryNameValidator.validate(dictionaryName);

        if (!validationResult.isValid()) {
            return new ServiceResponse(validationResult.getText(), validationResult.getUserEvent());
        }

        if (UserContext.getUser().getDictionaries().get(dictionaryName) != null) {
            return new ServiceResponse(
                    Text.DICTIONARY_WITH_THIS_NAME_ALREADY_EXISTS,
                    UserEvent.TRYING_TO_USE_EXISTING_NAME_FOR_DICTIONARY
            );
        }

        UserDictionary userDictionary = new UserDictionary(dictionaryName, source, target);
        UserContext.getUser().getDictionaries().put(dictionaryName, userDictionary);
        UserContext.getUser().setCurrentDictionary(userDictionary);

        return new ServiceResponse();
    }

    public ServiceResponse renameCurrentDictionary(String newDictionaryName) {
        ValidationResult validationResult = dictionaryNameValidator.validate(newDictionaryName);

        if (!validationResult.isValid()) {
            return new ServiceResponse(validationResult.getText(), validationResult.getUserEvent());
        }

        if (UserContext.getUser().getDictionaries().get(newDictionaryName) != null) {
            return new ServiceResponse(
                    Text.DICTIONARY_WITH_THIS_NAME_ALREADY_EXISTS,
                    UserEvent.TRYING_TO_USE_EXISTING_NAME_FOR_DICTIONARY
            );
        }

        UserDictionary userDictionary = UserContext.getUser().getCurrentDictionary();
        UserContext.getUser().getDictionaries().remove(userDictionary.getName());
        UserContext.getUser().getCurrentDictionary().setName(newDictionaryName);
        UserContext.getUser().getDictionaries().put(userDictionary.getName(), userDictionary);
        UserContext.getUser().setCurrentDictionary(userDictionary);

        return new ServiceResponse();
    }

    public void deleteCurrentDictionary() {
        UserContext.getUser().getDictionaries().remove(UserContext.getUser().getCurrentDictionary().getName());

        Optional<UserDictionary> newDictionary = UserContext.getUser().getDictionaries().values().stream().findFirst();
        newDictionary.ifPresent(userDictionary -> UserContext.getUser().setCurrentDictionary(userDictionary));
    }

    public void addWordToDictionary(Word word) {


        // FIX IT!
        addWordToDictionary(UserContext.getUser().getCurrentDictionary(), word);
        addWordToDictionary(UserContext.getUser().getDictionaries().get(UserContext.getUser().getCurrentDictionary().getName()), word);
    }

    public void addWordToDictionary(UserDictionary userDictionary, Word word) {
        // TODO Refactor it AND FIX IT!
        if (userDictionary.getWords().stream().map(UserWord::getWord).noneMatch(
                w -> {
                    Language source = userDictionary.getSourceLanguage();
                    Language target = userDictionary.getTargetLanguage();

                    return w.getTranslation(source).equalsIgnoreCase(word.getTranslation(source))
                            && w.getTranslation(target).equalsIgnoreCase(word.getTranslation(target));
                }
        )) {
            if (userDictionary.getSort().equals(Sort.OLD_TO_NEW)) {
                userDictionary.getWords().add(new UserWord(word));
            } else {
                userDictionary.getWords().add(0, new UserWord(word));
            }
        }
    }

    public void setQuizLength(int quizLength) {
        if (UserContext.getUser().getUserSettings() == null) {
            UserContext.getUser().setUserSettings(new UserSettings());
        }

        UserContext.getUser().getUserSettings().setQuizLength(quizLength);
    }

    public void setLanguageCode(String languageCode) {
        if (UserContext.getUser().getUserSettings() == null) {
            UserContext.getUser().setUserSettings(new UserSettings());
        }

        UserContext.getUser().getUserSettings().setLanguageCode(languageCode);
    }

    public UserProgress getUserProgress() {
        return UserProgress.builder()
                .languages(UserContext.getUser().getDictionaries().values().stream()
                        .map(UserDictionary::getTargetLanguage)
                        .distinct()
                        .collect(Collectors.toList()))
                .wordsCount(UserContext.getUser().getDictionaries().values().stream()
                        .map(UserDictionary::getWords)
                        .map(List::size)
                        .reduce(0, Integer::sum))
                .learnedWordsCount(UserContext.getUser().getDictionaries().values().stream()
                        .map(UserDictionary::getLearnedWordsCount)
                        .reduce(0, Integer::sum))
                .totalScore(getScore())
                .currentDictionary(UserContext.getUser().getCurrentDictionary())
                .build();
    }

    private static Float getScore() {
        int totalWordsCount = UserContext.getUser().getDictionaries().values().stream()
                .map(UserDictionary::getWords)
                .map(List::size)
                .reduce(0, Integer::sum);

        float totalQuizCount = UserContext.getUser().getDictionaries().values().stream()
                .flatMap(userDictionary -> userDictionary.getWords().stream())
                .map(UserWord::getQuizCount)
                .map(quizCount -> quizCount > UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT ? UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT : quizCount)
                .reduce(0, Integer::sum);

        float score = totalQuizCount / ((float) totalWordsCount * UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT);
        score *= 100;

        if (Float.isNaN(score)) {
            score = 0F;
        }

        return score;
    }

    public void fixMissingWordsForDictionary() {
        Language sourceLanguage = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
        Language targetLanguage = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

        // Fix for missed words (temporary?)
        UserContext.getUser().getCurrentDictionary().getWords().forEach(userWord -> {
            Word word = userWord.getWord();
            if (word.getFullTranslationWithEmoji(sourceLanguage, targetLanguage) == null
                    && word.getTranslation(sourceLanguage) != null
                    && word.getTranslation(targetLanguage) != null
            ) {
                log.info("Fixing missing word: " + word.getRussian() + " for user: " + UserContext.getUser().getName());
                Optional<Word> translatedWord = dictionary.translate(word.getRussian(), UserContext.getLanguageCode(), sourceLanguage, targetLanguage);
                translatedWord.ifPresent(userWord::setWord);
            }
        });
    }

    public void removeWordFromDictionary(String wordId, String dictionaryId) {
        // TODO Fix it!
        Optional<UserWord> userWord = UserContext.getUser().getDictionaries()
                .get(dictionaryId)
                .getWords()
                .stream()
                .filter(uw -> uw.getWord().getId().equals(wordId))
                .findFirst();
        userWord.ifPresent(word -> UserContext.getUser().getDictionaries()
                .get(dictionaryId)
                .getWords()
                .remove(word)
        );

        userWord = UserContext.getUser().getCurrentDictionary()
                .getWords()
                .stream()
                .filter(uw -> uw.getWord().getId().equals(wordId))
                .findFirst();
        userWord.ifPresent(word -> UserContext.getUser().getCurrentDictionary()
                .getWords()
                .remove(word)
        );
    }

    public void turnOffAllNotifications() {
        UserContext.getUser().getCurrentDictionary().turnOffNotification();
        UserContext.getUser().getDictionaries().values().forEach(UserDictionary::turnOffNotification);
    }
}
