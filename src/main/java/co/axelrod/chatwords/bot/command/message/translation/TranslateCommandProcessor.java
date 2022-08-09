package co.axelrod.chatwords.bot.command.message.translation;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.*;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.Dictionary;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexSpeechKit;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexSpellCheck;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexTranslate;
import co.axelrod.chatwords.dictionary.provider.yandex.model.translation.Translation;
import co.axelrod.chatwords.image.ShutterstockClient;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.*;
import co.axelrod.chatwords.storage.story.NoStory;
import co.axelrod.chatwords.util.HashUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TranslateCommandProcessor extends CommandProcessor<NoStory> {
    private static final int SENTENCE_LENGTH_LIMIT = 3500;
    private static final boolean NEW_WORD = true;

    private final WordRepository wordRepository;
    private final Dictionary dictionary;
    private final YandexSpellCheck yandexSpellCheck;
    private final YandexTranslate yandexTranslate;
    private final YandexSpeechKit yandexSpeechKit;
    private final ShutterstockClient shutterstockClient;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public TranslateCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            WordRepository wordRepository,
            Dictionary dictionary,
            YandexSpellCheck yandexSpellCheck,
            YandexTranslate yandexTranslate,
            YandexSpeechKit yandexSpeechKit,
            ShutterstockClient shutterstockClient,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.wordRepository = wordRepository;
        this.dictionary = dictionary;
        this.yandexTranslate = yandexTranslate;
        this.yandexSpellCheck = yandexSpellCheck;
        this.yandexSpeechKit = yandexSpeechKit;
        this.shutterstockClient = shutterstockClient;
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
    }

    @Override
    protected void processRequest() {
//        senderService.sendTypingAction();

        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
//        if (callback != null && callback.equals(Callback.CHANGE_TRANSLATION_LANGUAGE)) {
//            ChangeTranslationLanguageCallback changeTranslationLanguageCallback = new ChangeTranslationLanguageCallback(UserContext.getRequest());
//            Language targetLanguage = Language.getLanguageById(changeTranslationLanguageCallback.getTargetLanguageId());
//
//            UserContext.setRequest(UserContext.getUser().getLastTranslation());
//            translateSentence(
//                    UserContext.getUser().getLastTranslationSourceLanguage(),
//                    targetLanguage
//            );
//            return;
//        }

//        if (callback != null && callback.equals(Callback.CHANGE_TRANSLATION_MODE)) {
//            ChangeTranslationModeCallback changeTranslationModeCallback = new ChangeTranslationModeCallback(UserContext.getRequest());
//            UserContext.getUser().setShowSourceText(changeTranslationModeCallback.getShowSourceText().equals(Boolean.TRUE.toString()));
//            UserContext.setRequest(UserContext.getUser().getLastTranslation());
//            translateSentence(
//                    UserContext.getUser().getLastTranslationSourceLanguage(),
//                    UserContext.getUser().getLastTranslationTargetLanguage()
//            );
//            return;
//        }

        if (callback != null && callback.equals(Callback.ADD_WORD_TO_DICTIONARY)) {
            AddWordToDictionaryCallback addWordToDictionaryCallback = new AddWordToDictionaryCallback(UserContext.getRequest());
            String wordId = addWordToDictionaryCallback.getWordId();
            Word word = wordRepository.findById(wordId).get();
            userService.addWordToDictionary(word);

            Language source = Language.getLanguageById(addWordToDictionaryCallback.getSource());
            Language target = Language.getLanguageById(addWordToDictionaryCallback.getTarget());

            UserContext.setRequest(UserContext.getUser().getLastTranslation());

            UserContext.setCustomCallbackAlertText(
                    String.format(
                            UserContext.getText(Text.WORD_HAS_BEEN_ADDED_TO_DICTIONARY),
                            UserContext.getUser().getCurrentDictionary().getName(),
                            UserContext.getUser().getCurrentDictionary().getSourceLanguage().getEmoji(),
                            UserContext.getUser().getCurrentDictionary().getTargetLanguage().getEmoji()
                    )
            );

            analytics.log(UserEvent.ADD_WORD_TO_DICTIONARY);
            UserContext.getUser().getCurrentDayActivity().wordAdded();
            senderService.updateMessage(word.getFormattedTranslation(source, target),
                    translatedWordKeyboard(word, !NEW_WORD, true, source, target));
            return;
        }

        UserContext.setRequest(sanitizeUserInput(UserContext.getRequest()));
        UserContext.setRequest(yandexSpellCheck.correctSpelling(
                UserContext.getRequest(),
                UserContext.getUser().getCurrentDictionary().getSourceLanguage(),
                UserContext.getUser().getCurrentDictionary().getTargetLanguage())
        );

        // Translate phrase or sentence
        if (UserContext.getRequest().contains(" ") && UserContext.getRequest().split(" ").length > 1) {
            analytics.log(UserEvent.SENTENCE_TRANSLATION_UNAVAILABLE);
            showDictionaryCommandProcessor.processRequest();
            senderService.updateMessage(String.format(UserContext.getMessagesBundle().getString(Text.SENTENCE_TRANSLATION_UNAVAILABLE.getValue()), UserContext.getRequest()));
//            translateSentence();
        } else {
            translateWord();
        }
    }

    private void translateWord() {
        // Translate word
        UserDictionary userDictionary = UserContext.getUser().getCurrentDictionary();
        Optional<Word> word = dictionary.translate(UserContext.getRequest(), UserContext.getLanguageCode(), userDictionary.getSourceLanguage(), userDictionary.getTargetLanguage())
                .or(() -> dictionary.translate(UserContext.getRequest(), UserContext.getLanguageCode(), userDictionary.getTargetLanguage(), userDictionary.getSourceLanguage()));

        if (word.isEmpty()) {
//            Language sourceLanguage = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//            Language targetLanguage = UserContext.getUser().getCurrentDictionary().getTargetLanguage();
//
//            Translation translation = yandexTranslate.translate(UserContext.getRequest(), sourceLanguage, targetLanguage);
//
//            if (translation.getDetectedLanguageCode() != null) {
//                sourceLanguage = Language.getLanguageById(translation.getDetectedLanguageCode()).equals(sourceLanguage)
//                        ? sourceLanguage
//                        : targetLanguage;
//            }
//
//            targetLanguage = sourceLanguage.equals(UserContext.getUser().getCurrentDictionary().getSourceLanguage())
//                    ? UserContext.getUser().getCurrentDictionary().getTargetLanguage()
//                    : UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//
//            if (translation.getText() != null) {
//                senderService.updateMessage(
//                        sourceLanguage.getEmoji() + targetLanguage.getEmoji() + "\n"
//                                + "<b>" + UserContext.getRequest() + "</b>"
//                                + "\n" + "- "
//                                + translation.getText()
//                                + "\n"
//                                + UserContext.getText(Text.NOT_FULL_TRANSLATION)
//                        ,
//                        showDictionaryKeyboard()
//                );
//            } else {
                analytics.log(UserEvent.WORD_NOT_FOUND);
                showDictionaryCommandProcessor.processRequest();
                senderService.updateMessage(String.format(UserContext.getMessagesBundle().getString(Text.TRANSLATION_NOT_FOUND.getValue()), UserContext.getRequest()));
//            }
        } else {
            Language source = getLanguageByMorph(UserContext.getRequest(), word.get(), userDictionary.getSourceLanguage(), userDictionary.getTargetLanguage());
            Language target = userDictionary.getSourceLanguage().equals(source) ? userDictionary.getTargetLanguage() : userDictionary.getSourceLanguage();

            analytics.log(UserEvent.WORD_TRANSLATED);
            UserContext.getUser().getCurrentDayActivity().wordTranslated();

            boolean newWord = NEW_WORD;

            // TODO GET RID OF IT!!! YOU STORE COPIES OF WORDS WITH DIFFERENT ID's!
            // Fix for deleting already existing word
            Optional<Word> finalWord = word;
            Word wordFromDictionary = userDictionary.getWords()
                    .stream().map(UserWord::getWord)
                    .filter(userWord -> userWord.getTranslation(source).equalsIgnoreCase(finalWord.get().getTranslation(source))
                            && userWord.getTranslation(target).equalsIgnoreCase(finalWord.get().getTranslation(target)))
                    .findFirst().orElse(null);
            if (wordFromDictionary != null) {
                word = Optional.of(wordFromDictionary);
                newWord = !NEW_WORD;
            }

            // Отправка голоса
            InputFile pronunciationFile = null;
            if (source.equals(Language.ENGLISH) || target.equals(Language.ENGLISH)) {
                pronunciationFile = yandexSpeechKit.getSpeech(word.get().getEnglish());
            }

            if (pronunciationFile != null) {
                senderService.sendVoice(word.get().getFormattedTranslation(source, target),
                        pronunciationFile,
                        translatedWordKeyboard(word.get(), newWord, true, source, target));
            } else {
                senderService.updateMessage(word.get().getFormattedTranslation(source, target),
                        translatedWordKeyboard(word.get(), newWord, true, source, target));
            }

//            // Отправка изображения
//            Language imageQueryLanguage = source;
//            if (imageQueryLanguage.equals(Language.UKRAINIAN)) {
//                imageQueryLanguage = target;
//            }
//            String imageUrl = shutterstockClient.getImageUrl(word.get().getTranslation(imageQueryLanguage), imageQueryLanguage);
//
//            if (imageUrl == null) {
//                senderService.updateMessage(
//                        word.get().getFormattedTranslation(source, target),
//                        translatedWordKeyboard(word.get(), newWord, true, source, target));
//            } else {
//                senderService.sendPhoto(
//                        word.get().getFormattedTranslation(source, target),
//                        imageUrl,
//                        translatedWordKeyboard(word.get(), newWord, true, source, target));
//            }
        }
    }

//    private void translateSentence() {
//        Language sourceLanguage = yandexTranslate.determineLanguage(
//                UserContext.getRequest().substring(0, Math.min(UserContext.getRequest().length(), 100)),
//                UserContext.getUser().getCurrentDictionary().getSourceLanguage(),
//                UserContext.getUser().getCurrentDictionary().getTargetLanguage()
//        );
//        Language targetLanguage = UserContext.getUser().getLastTranslationTargetLanguage();
//
//        if (targetLanguage == null || targetLanguage.equals(sourceLanguage)) {
//            boolean sourceLanguageMatchesDictionary =
//                    sourceLanguage.equals(UserContext.getUser().getCurrentDictionary().getSourceLanguage()) ||
//                            sourceLanguage.equals(UserContext.getUser().getCurrentDictionary().getTargetLanguage());
//            if (sourceLanguageMatchesDictionary) {
//                targetLanguage = sourceLanguage.equals(UserContext.getUser().getCurrentDictionary().getSourceLanguage())
//                        ? UserContext.getUser().getCurrentDictionary().getTargetLanguage()
//                        : UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//            } else {
//                // Если текущий словарь EN-RU, а пришел IT - переводим в первую очередь на RU
//                targetLanguage = UserContext.getUser().getDefaultFromLanguage();
//            }
//        }
//        translateSentence(sourceLanguage, targetLanguage);
//    }

//    private void translateSentence(Language sourceLanguage, Language targetLanguage) {
//        boolean longSentence = UserContext.getRequest().length() > SENTENCE_LENGTH_LIMIT;
//
//        if (longSentence) {
//            analytics.log(UserEvent.TOO_LONG_SENTENCE_TRANSLATION_ATTEMPT);
//            UserContext.setRequest(UserContext.getRequest().substring(0, SENTENCE_LENGTH_LIMIT));
//        }
//
//        UserContext.getUser().setLastTranslation(UserContext.getRequest());
//        UserContext.getUser().setLastTranslationSourceLanguage(sourceLanguage);
//        UserContext.getUser().setLastTranslationTargetLanguage(targetLanguage);
//        Translation translation = yandexTranslate.translate(UserContext.getRequest(), sourceLanguage, targetLanguage);
//        UserContext.getUser().getCurrentDayActivity().sentenceTranslated();
//        analytics.log(UserEvent.SENTENCE_TRANSLATION);
//
//        String message;
//
//        if (UserContext.getUser().getShowSourceText() == null || UserContext.getUser().getShowSourceText()) {
//            message = translation.getText();
//        } else {
//            message = UserContext.getText(Text.COPYING_TEXT_HINT) + "\n\n"
//                    + "<code>" + translation.getText() + "</code>";
//        }
//
//        if (longSentence) {
//            message += "\n\n" + UserContext.getText(Text.CANNOT_TRANSLATE_LONG_SENTENCE);
//        }
//
//        UserContext.getUser().setLastTranslationResult(translation.getText());
//        senderService.updateMessage(message, translatedSentenceKeyboard(sourceLanguage));
//    }

    public static Language getLanguageByMorph(String guess, Word word, Language source, Language target) {
        // Check for source
        int charactersInSource = 0;
        String sourceTranslation = word.getTranslation(source);
        for (byte characterInSource : sourceTranslation.getBytes(StandardCharsets.UTF_8)) {
            for (byte characterInGuess : guess.getBytes(StandardCharsets.UTF_8)) {
                if (characterInSource == characterInGuess) {
                    charactersInSource++;
                }
            }
        }

        // Check for target
        int charactersInTarget = 0;
        String targetTranslation = word.getTranslation(target);
        for (byte characterInTarget : targetTranslation.getBytes(StandardCharsets.UTF_8)) {
            for (byte characterInGuess : guess.getBytes(StandardCharsets.UTF_8)) {
                if (characterInTarget == characterInGuess) {
                    charactersInTarget++;
                }
            }
        }

        return charactersInSource > charactersInTarget ? source : target;
    }

    @Override
    protected NoStory getStory() {
        return null;
    }

    private String sanitizeUserInput(String input) {
        String sanitizedInput = input;

        List<String> emojis = Arrays.stream(Language.values()).map(Language::getEmoji).collect(Collectors.toList());
        for (String emoji : emojis) {
            sanitizedInput = sanitizedInput.replace(emoji, "");
        }

        sanitizedInput = sanitizedInput.strip();

        return sanitizedInput;
    }

    public static InlineKeyboardMarkup translatedWordKeyboard(Word word, boolean newWord, boolean detailedButton, Language source, Language target) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        // TODO Проблема. Для некоторых вариантов может не быть обратного перевода.
        List<String> variants = word.getRawTranslation(source, target)
                .getDef().stream().flatMap(definition -> definition.getTr().stream().limit(2))
                .map(co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Translation::getText)
                .filter(text -> !text.equals(word.getTranslation(target)))
                .limit(3)
                .collect(Collectors.toList());

        variants.forEach(
                variant -> builder.keyboardRow(
                        Collections.singletonList(InlineKeyboardButton.builder()
                                .text(target.getEmoji() + " " + variant)
                                .callbackData(ChangeWordTranslationCallback.builder()
                                        .wordId(word.getId())
                                        .translationHash(HashUtil.getHash(variant))
                                        .languageFrom(source.getValue())
                                        .languageTo(target.getValue())
                                        .build()
                                        .code()
                                )
                                .build()
                        )
                )
        );

        if (detailedButton) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.DETAILED_TRANSLATION))
                                    .callbackData(DetailedWordTranslationCallback.builder()
                                            .wordId(word.getId())
                                            .languageFrom(source.getValue())
                                            .languageTo(target.getValue())
                                            .build()
                                            .code())
                                    .build()
                    )
            );
        }

        if (newWord) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.ADD_WORD_TO_DICTIONARY))
                                    .callbackData(AddWordToDictionaryCallback.builder()
                                            .wordId(word.getId())
                                            .source(source.getValue())
                                            .target(target.getValue())
                                            .build()
                                            .code())
                                    .build()
                    )
            );
        } else {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.DELETE_FROM_DICTIONARY))
                                    .callbackData(DeleteWordCallback.builder()
                                            .wordId(word.getId())
                                            .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                            .build()
                                            .code())
                                    .build()
                    )
            );
        }

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.DICTIONARY_BUTTON))
                        .callbackData(Callback.SHOW_DICTIONARY.getValue())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_BUTTON))
                        .callbackData(UserContext.getText(Text.EXIT))
                        .build()
        ));

        return builder.build();
    }

    private InlineKeyboardMarkup showDictionaryKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.SHOW_DICTIONARY))
                                .callbackData(Callback.SHOW_DICTIONARY.getValue())
                                .build()
                ))
                .build();
    }

    private InlineKeyboardMarkup translatedSentenceKeyboard(Language source) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(
                Arrays.stream(Language.values())
                        .filter(language -> !language.equals(source))
                        .map(language -> InlineKeyboardButton.builder()
                                .text(language.getEmoji())
                                .callbackData(ChangeTranslationLanguageCallback.builder()
                                        .targetLanguageId(language.getValue())
                                        .build()
                                        .code())
                                .build()).collect(Collectors.toList()));

        Language target = UserContext.getUser().getLastTranslationTargetLanguage();

        boolean languagePairMatchesCurrentDictionary = false;

        if (source != null) {
            languagePairMatchesCurrentDictionary =
                    (source.equals(UserContext.getUser().getCurrentDictionary().getSourceLanguage())
                            && target.equals(UserContext.getUser().getCurrentDictionary().getTargetLanguage()))
                            || (source.equals(UserContext.getUser().getCurrentDictionary().getTargetLanguage())
                            && target.equals(UserContext.getUser().getCurrentDictionary().getSourceLanguage()));
        }

        if (languagePairMatchesCurrentDictionary) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.ADD_WORD_TO_DICTIONARY))
                                    .callbackData(Callback.SENTENCE_TO_DICTIONARY.getValue())
                                    .build()
                    )
            );
        }

        if (UserContext.getUser().getShowSourceText() == null || UserContext.getUser().getShowSourceText()) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.HIDE_SOURCE_TEXT))
                                    .callbackData(ChangeTranslationModeCallback.builder()
                                            .showSourceText(Boolean.FALSE.toString())
                                            .build()
                                            .code())
                                    .build()
                    )
            );
        } else {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.SHOW_SOURCE_TEXT))
                                    .callbackData(ChangeTranslationModeCallback.builder()
                                            .showSourceText(Boolean.TRUE.toString())
                                            .build()
                                            .code())
                                    .build()
                    )
            );
        }

        builder.keyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.MENU_BUTTON))
                                        .callbackData(UserContext.getText(Text.EXIT))
                                        .build()
                        )
                )
                .build();

        return builder.build();
    }
}
