package co.axelrod.chatwords.bot.command.message.translation;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.MainMenuCommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.AddSentenceWordToDictionaryCallback;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.Dictionary;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserDictionary;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.Word;
import co.axelrod.chatwords.storage.story.SentenceToDictionaryStory;
import co.axelrod.chatwords.util.HashUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SentenceToDictionaryCommandProcessor extends CommandProcessor<SentenceToDictionaryStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    private final Dictionary dictionary;

    private static final int ELEMENTS_ON_SCREEN = 8;

    protected SentenceToDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            MainMenuCommandProcessor mainMenuCommandProcessor,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor,
            Dictionary dictionary
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.mainMenuCommandProcessor = mainMenuCommandProcessor;
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
        this.dictionary = dictionary;
    }

    @Override
    protected void processRequest() {
        // Возврат к словарю
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.SHOW_DICTIONARY.getValue())) {
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        SentenceToDictionaryStory story = getStory();

        String message = UserContext.getUser().getLastTranslationSourceLanguage().getEmoji() + " "
                + UserContext.getUser().getLastTranslation() + "\n"
                + UserContext.getUser().getLastTranslationTargetLanguage().getEmoji() + " "
                + UserContext.getUser().getLastTranslationResult() + "\n\n"
                + UserContext.getText(Text.ADD_WORDS_FROM_SENTENCE_BY_CLICK);

        if (story == null) {
            UserContext.getUser().setSentenceToDictionaryStory(new SentenceToDictionaryStory());
            parseTranslationToWords();
            analytics.log(UserEvent.USER_OPENED_ADD_TRANSLATION_BY_WORDS);
        }

        List<SentenceToDictionaryStory.WordFromSentence> values = getPage(getStory().getWords());

        if (UserContext.getRequest().equals(Callback.NEXT_PAGE.getValue())) {
            analytics.log(UserEvent.ADD_WORD_FROM_SENTENCE_TO_DICTIONARY_NEXT_PAGE);
            values = nextPage(getStory().getWords());
        }
        if (UserContext.getRequest().equals(Callback.PREVIOUS_PAGE.getValue())) {
            analytics.log(UserEvent.ADD_WORD_FROM_SENTENCE_TO_DICTIONARY_PREVIOUS_PAGE);
            values = previousPage(getStory().getWords());
        }

        // Обновить клавиатуру
        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.equals(Callback.ADD_SENTENCE_WORD_TO_DICTIONARY)) {
            AddSentenceWordToDictionaryCallback addSentenceWordToDictionaryCallback = new AddSentenceWordToDictionaryCallback(UserContext.getRequest());

            SentenceToDictionaryStory.WordFromSentence wordFromSentence =
                    getStory().getWords().stream()
                            .filter(w ->
                                    addSentenceWordToDictionaryCallback.getWordHash()
                                            .equals(HashUtil.getHash(w.getWord()))
                            )
                            .findAny()
                            .get();
            if (wordFromSentence.getAddedToDictionary()) {
                UserContext.setCustomCallbackAlertText(
                        String.format(UserContext.getText(Text.WORD_HAS_BEEN_ALREADY_ADDED_TO_DICTIONARY),
                                UserContext.getUser().getCurrentDictionary().getName())
                );
            } else {
                boolean wordHasBeenAdded = addWordToDictionary(wordFromSentence.getWord());

                if (wordHasBeenAdded) {
                    wordFromSentence.setAddedToDictionary(true);
                    UserContext.setCustomCallbackAlertText(
                            String.format(
                                    UserContext.getText(Text.WORD_HAS_BEEN_ADDED_TO_DICTIONARY),
                                    UserContext.getUser().getCurrentDictionary().getName(),
                                    UserContext.getUser().getCurrentDictionary().getSourceLanguage().getEmoji(),
                                    UserContext.getUser().getCurrentDictionary().getTargetLanguage().getEmoji()
                            )
                    );
                } else {
                    wordFromSentence.setAddedToDictionary(false);
                    UserContext.setCustomCallbackAlertText(
                            String.format(
                                    UserContext.getText(Text.TRANSLATION_OF_SENTENCE_WORD_NOT_FOUND),
                                    wordFromSentence.getWord()
                            )
                    );
                }
            }

            senderService.updateMessage(message, addTranslationWordsToDictionaryKeyboard(values));
            return;
        }
        // Возврат в меню
        if (callback != null && callback.getId().equals(Callback.CANCEL.getId())) {
            UserContext.getUser().clearStories();
            mainMenuCommandProcessor.processRequest();
            return;
        }

        senderService.updateMessage(message, addTranslationWordsToDictionaryKeyboard(values));
    }

    public List<SentenceToDictionaryStory.WordFromSentence> previousPage(List<SentenceToDictionaryStory.WordFromSentence> words) {
        if (getStory().getCurrentOffset().equals(ELEMENTS_ON_SCREEN)) {
            getStory().setCurrentOffset(0);
        } else {
            getStory().setCurrentOffset(getStory().getCurrentOffset() - ELEMENTS_ON_SCREEN);
        }
        return getPage(words);
    }

    public List<SentenceToDictionaryStory.WordFromSentence> nextPage(List<SentenceToDictionaryStory.WordFromSentence> words) {
        getStory().setCurrentOffset(Math.min(getStory().getCurrentOffset() + ELEMENTS_ON_SCREEN, words.size()));
        return getPage(words);
    }

    public List<SentenceToDictionaryStory.WordFromSentence> getPage(List<SentenceToDictionaryStory.WordFromSentence> words) {
        return words.stream()
                .skip(getStory().getCurrentOffset())
                .limit(ELEMENTS_ON_SCREEN)
                .collect(Collectors.toList());
    }

    private boolean addWordToDictionary(String wordToTranslate) {
        // Translate word
        UserDictionary userDictionary = UserContext.getUser().getCurrentDictionary();
        Optional<Word> word = dictionary.translate(
                        wordToTranslate,
                        UserContext.getLanguageCode(),
                        userDictionary.getSourceLanguage(),
                        userDictionary.getTargetLanguage()
                )
                .or(() -> dictionary.translate(
                        wordToTranslate,
                        UserContext.getLanguageCode(),
                        userDictionary.getTargetLanguage(),
                        userDictionary.getSourceLanguage())
                );

        if (word.isEmpty()) {
            analytics.log(UserEvent.WORD_NOT_FOUND);
            return false;
        } else {
            Language source = TranslateCommandProcessor.getLanguageByMorph(UserContext.getRequest(), word.get(), userDictionary.getSourceLanguage(), userDictionary.getTargetLanguage());
            Language target = userDictionary.getSourceLanguage().equals(source) ? userDictionary.getTargetLanguage() : userDictionary.getSourceLanguage();

            analytics.log(UserEvent.WORD_TRANSLATED);
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
            }

            // Сохранить слово в словаре
            UserContext.getUser().getCurrentDayActivity().wordAdded();
            analytics.log(UserEvent.ADD_WORD_FROM_SENTENCE_TO_DICTIONARY);
            userService.addWordToDictionary(word.get());
            return true;
        }
    }

    @Override
    protected SentenceToDictionaryStory getStory() {
        return UserContext.getUser().getSentenceToDictionaryStory();
    }

    private void parseTranslationToWords() {
        String[] tokens = UserContext.getUser().getLastTranslationResult().split(" ");
        /** Фильтруем токены:
         * + убираем лишние знаки
         * + убираем дубликаты
         * + убираем слова короче 2 символов
         * + отмечаем слова, присутствующие в словаре, как добавленные
         */

        List<String> words = Arrays.stream(tokens)
                .map(word -> word.replaceAll("\\p{Punct}", ""))
                .distinct()
                .filter(word -> word.length() > 2).collect(Collectors.toList());

        List<String> currentDictionaryWords = UserContext.getUser().getCurrentDictionary().getWords().stream()
                .map(UserWord::getWord)
                .map(word -> word.getTranslation(UserContext.getUser().getLastTranslationTargetLanguage()))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        getStory().setWords(words.stream()
                .map(word -> new SentenceToDictionaryStory.WordFromSentence(word, currentDictionaryWords.contains(word.toLowerCase())))
                .collect(Collectors.toList())
        );
    }

    protected List<InlineKeyboardButton> getPagingKeyboardRow() {
        boolean hasLeftButton;
        boolean hasRightButton;

        int currentPage = getStory().getCurrentOffset() / ELEMENTS_ON_SCREEN;
        int totalPages = getStory().getWords().size() / ELEMENTS_ON_SCREEN;
        if (totalPages * ELEMENTS_ON_SCREEN < getStory().getWords().size()) {
            totalPages++;
        }

        hasLeftButton = currentPage != 0;
        hasRightButton = currentPage < totalPages - 1;

        InlineKeyboardButton leftButton = InlineKeyboardButton.builder()
                .text(UserContext.getText(Text.PREVIOUS_PAGE))
                .callbackData(Callback.PREVIOUS_PAGE.getValue())
                .build();

        InlineKeyboardButton rightButton = InlineKeyboardButton.builder()
                .text(UserContext.getText(Text.NEXT_PAGE))
                .callbackData(Callback.NEXT_PAGE.getValue())
                .build();

        if (hasLeftButton && hasRightButton) {
            return Arrays.asList(leftButton, rightButton);
        }

        if (hasLeftButton) {
            return Collections.singletonList(leftButton);
        }
        if (hasRightButton) {
            return Collections.singletonList(rightButton);
        }

        return Collections.emptyList();
    }

    private InlineKeyboardMarkup addTranslationWordsToDictionaryKeyboard(List<SentenceToDictionaryStory.WordFromSentence> words) {
        List<InlineKeyboardButton> buttons = words.stream()
                .map(wordFromSentence -> InlineKeyboardButton.builder()
                        .text(wordFromSentence.getAddedToDictionary()
                                ? "✅ " + wordFromSentence.getWord()
                                : "\uD83C\uDD95 " + wordFromSentence.getWord()
                        )
                        .callbackData(AddSentenceWordToDictionaryCallback.builder()
                                .wordHash(HashUtil.getHash(wordFromSentence.getWord()))
                                .source(UserContext.getUser().getLastTranslationSourceLanguage().getValue())
                                .target(UserContext.getUser().getLastTranslationTargetLanguage().getValue())
                                .build()
                                .code()
                        )
                        .build())
                .collect(Collectors.toList());

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        int pairRows = buttons.size() / 2;
        boolean lastRow = buttons.size() % 2 == 1;

        IntStream.range(0, pairRows).forEach(row ->
                builder.keyboardRow(
                        Arrays.asList(buttons.get(row * 2), buttons.get(row * 2 + 1))
                )
        );
        if (lastRow) {
            builder.keyboardRow(Collections.singletonList(
                    buttons.get(buttons.size() - 1)
            ));
        }

        if (!getPagingKeyboardRow().isEmpty()) {
            builder.keyboardRow(getPagingKeyboardRow());
        }

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.SHOW_DICTIONARY))
                        .callbackData(Callback.SHOW_DICTIONARY.getValue())
                        .build()
        ));

        builder.keyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.MENU_BUTTON))
                                        .callbackData(CancelCallback.builder().build().code())
                                        .build()
                        )
                )
                .build();

        return builder.build();
    }
}
