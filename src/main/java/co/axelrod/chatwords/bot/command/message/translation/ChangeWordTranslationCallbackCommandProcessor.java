package co.axelrod.chatwords.bot.command.message.translation;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.ChangeWordTranslationCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionary;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Translation;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.YandexDictionaryResponse;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.*;
import co.axelrod.chatwords.storage.story.NoStory;
import co.axelrod.chatwords.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ChangeWordTranslationCallbackCommandProcessor extends CommandProcessor<NoStory> {
    private final WordRepository wordRepository;
    private final YandexDictionary yandexDictionary;

    public ChangeWordTranslationCallbackCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            WordRepository wordRepository,
            YandexDictionary yandexDictionary
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.wordRepository = wordRepository;
        this.yandexDictionary = yandexDictionary;
    }

    @Override
    protected void processRequest() {
        ChangeWordTranslationCallback changeWordTranslationCallback = new ChangeWordTranslationCallback(UserContext.getRequest());

        Language source = Language.getLanguageById(changeWordTranslationCallback.getLanguageFrom());
        Language target = Language.getLanguageById(changeWordTranslationCallback.getLanguageTo());

        Word word = getWordToChange(changeWordTranslationCallback);
        if (word == null) {
            log.error("Word to change not found");
            return;
        }

        String variant = getWordVariantByHash(word, changeWordTranslationCallback.getTranslationHash(), source, target);
        if (variant == null) {
            log.error("Variant of word not found");
            return;
        }

        Word wordWithNewVariant = getWordForVariant(word, variant, source, target);

        boolean wordFromCurrentDictionary = wordExistsInCurrentDictionary(wordWithNewVariant);

        analytics.log(UserEvent.CHANGED_WORD_TRANSLATION_VARIANT);
        senderService.updateMessage(
                wordWithNewVariant.getFormattedTranslation(source, target),
                TranslateCommandProcessor.translatedWordKeyboard(wordWithNewVariant,
                        !wordFromCurrentDictionary,
                        true,
                        source,
                        target)
        );
    }

    private boolean wordExistsInCurrentDictionary(Word word) {
        return UserContext.getUser().getCurrentDictionary().getWords().stream()
                .anyMatch(userWord -> userWord.getWord().getId().equals(word.getId()));
    }

    private Word getWordToChange(ChangeWordTranslationCallback callback) {
        // Поиск в текущем словаре
        Word wordInCurrentDictionary = getWordById(UserContext.getUser().getCurrentDictionary(), callback.getWordId());
        if (wordInCurrentDictionary != null) {
            return wordInCurrentDictionary;
        }

        // Слово отсутствует в текущем словаре, поиск слова по общему словарю
        return wordRepository.findById(callback.getWordId()).orElse(null);
    }

    private Word getWordById(UserDictionary userDictionary, String wordId) {
        return userDictionary.getWords().stream()
                .map(UserWord::getWord)
                .filter(word -> wordId.equals(word.getId()))
                .findFirst().orElse(null);
    }

    private String getWordVariantByHash(Word word, String variantHash, Language source, Language target) {
        return word.getRawTranslation(source, target).getDef().stream()
                .flatMap(definition -> definition.getTr().stream())
                .map(Translation::getText)
                .filter(translation -> HashUtil.getHash(translation).equals(variantHash))
                .findFirst().orElse(null);
    }

    private Word getWordForVariant(Word word, String variant, Language source, Language target) {
        // Проверяем, есть ли в общем словаре слово с таким вариантом перевода
        Word wordFromRepository = wordRepository.findWords(word.getTranslation(source), source)
                .stream()
                .filter(foundWord -> foundWord.getTranslation(target) != null)
                .filter(foundWord -> foundWord.getTranslation(target).equals(variant))
                .findFirst().orElse(null);

        // Слово найдено в общем словаре
        if (wordFromRepository != null) {
            return wordFromRepository;
        }

        // Слово не найдено в общем словаре
        // Создаем новое слово
        Word wordWithNewVariant = new Word(word);
        wordWithNewVariant.setTranslation(variant, target);

        // Обновляем приоритет варианта перевода для source-target
        YandexDictionaryResponse yandexDictionaryResponse = wordWithNewVariant.getRawTranslation(source, target);
        updateTranslationPriority(yandexDictionaryResponse, variant);
        wordWithNewVariant.setFullTranslation(source, target, yandexDictionaryResponse);

        // Вариант перевода может отличаться (например, прыжок - jump - vaulting - прыжки)
        // Обновляем перевод source
        String translationOfVariant = getTranslationOfVariant(yandexDictionaryResponse, variant);
        wordWithNewVariant.setTranslation(translationOfVariant, source);

        // Поиск перевода для варианта
        Optional<YandexDictionaryResponse> translationForTargetToSource = yandexDictionary.callYandexDictionary(variant, UserContext.getLanguageCode(), target, source);
        // Перевода может не быть, обновляем только при наличии (чем ухудшаем качество обратного перевода, но это сознательно)
        if (translationForTargetToSource.isPresent()) {
            yandexDictionaryResponse = translationForTargetToSource.get();
            updateTranslationPriority(yandexDictionaryResponse, wordWithNewVariant.getTranslation(source));
            wordWithNewVariant.setFullTranslation(target, source, yandexDictionaryResponse);
        }

        // Сохраняем новый вариант перевода
        wordWithNewVariant = wordRepository.save(wordWithNewVariant);

        return wordWithNewVariant;
    }

    private String getTranslationOfVariant(YandexDictionaryResponse yandexDictionaryResponse, String variant) {
        // Не проверяем на null, так как перевод гарантированно есть
        return yandexDictionaryResponse.getDef().stream()
                .filter(definition -> definition.getTr().stream()
                        .filter(translation -> translation.getText().equals(variant)).count() > 0
                )
                .findFirst().get().getText();
    }

    private void updateTranslationPriority(YandexDictionaryResponse yandexDictionaryResponse, String variant) {
        Optional<Translation> optionalTopPriorityTranslation = yandexDictionaryResponse.getDef().stream()
                .flatMap(definition -> definition.getTr().stream())
                .filter(translation -> translation.getText().equals(variant))
                .findFirst();

        if (optionalTopPriorityTranslation.isPresent()) {
            // Меняем приоритет искомого слова
            Translation topPriorityTranslation = optionalTopPriorityTranslation.get();
            topPriorityTranslation.setAdditionalProperty("fr", 1000);

            // Сортируем переводы внутри каждого определения по частоте использования
            yandexDictionaryResponse.getDef().forEach(definition -> definition.getTr().sort((translation1, translation2) -> {
                Integer firstFrequency = (Integer) translation1.getAdditionalProperties().get("fr");
                Integer secondFrequency = (Integer) translation2.getAdditionalProperties().get("fr");

                if (firstFrequency != null && secondFrequency != null) {
                    return secondFrequency.compareTo(firstFrequency);
                } else {
                    return 0;
                }
            }));

            // Сортируем перевод по частоте использования
            yandexDictionaryResponse.getDef().sort((firstDefinition, secondDefinition) -> {
                Integer firstMaxFrequency = (Integer) firstDefinition.getTr().get(0).getAdditionalProperties().get("fr");
                Integer secondMaxFrequency = (Integer) secondDefinition.getTr().get(0).getAdditionalProperties().get("fr");

                if (firstMaxFrequency != null && secondMaxFrequency != null) {
                    return secondMaxFrequency.compareTo(firstMaxFrequency);
                } else {
                    return 0;
                }
            });

            // Возвращаем обычный приоритет искомого слова
            topPriorityTranslation.setAdditionalProperty("fr", 10);
        }
    }

    @Override
    protected NoStory getStory() {
        return null;
    }
}
