package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.QuizDeleteWordCallback;
import co.axelrod.chatwords.bot.model.callback.WordActionCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.service.quiz.model.WordAction;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.story.QuizStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public abstract class AbstractChooseWordsProcessor extends CommandProcessor<QuizStory> {
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected final QuizService quizService;

    protected AbstractChooseWordsProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            QuizService quizService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.quizService = quizService;
    }

    @Override
    protected void processRequest() {
        if (getStory().getAvailableWords() == null) {
            setAvailableQuizWords();

            // Добавляем пустой список слов для квиза
            getStory().setQuizWords(new ArrayList<>());

            if (getStory().getAvailableWords().isEmpty()) {
                getStory().setQuizCompleted(true);
                quizCommandProcessor.processRequest();
                return;
            }
        }

        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());

        if (callback != null && callback.equals(Callback.QUIZ_WORD_ACTION)) {
            WordActionCallback wordActionCallback = new WordActionCallback(UserContext.getRequest());
            WordAction wordAction = WordAction.fromValue(wordActionCallback.getType());

            switch (wordAction) {
                case ADD_WORD_TO_QUIZ: {
                    quizService.addWordToQuiz(wordActionCallback.getWordId());
                    quizService.removeWordFromAvailableWords(wordActionCallback.getWordId());
                }
                break;
                case SKIP_WORD: {
                    quizService.removeWordFromAvailableWords(wordActionCallback.getWordId());
                }
                break;
            }
        }

        // Если все слова добавлены - начинаем квиз
        int wordsLeftToAdd = quizService.getQuizLength() - getStory().getQuizWords().size();
        if (wordsLeftToAdd == 0) {
            startQuiz();
            return;
        }

        if (callback != null && callback.equals(Callback.QUIZ_REMOVE_WORD)) {
            QuizDeleteWordCallback quizDeleteWordCallback = new QuizDeleteWordCallback(UserContext.getRequest());
            userService.removeWordFromDictionary(quizDeleteWordCallback.getWordId(), quizDeleteWordCallback.getDictionaryId());
            quizService.removeWordFromAvailableWords(quizDeleteWordCallback.getWordId());
        }

        // Еще остались слова для добавления
        Optional<UserWord> userWord = quizService.getNextAvailableWordForQuiz();

        // Нашли слово для добавления - предлагаем пользователю
        if (userWord.isPresent()) {
            Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
            Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

            senderService.updateMessage(
                    userWord.get().getWord().getFormattedTranslation(source, target),
                    wordActionKeyboard(userWord.get()));
            return;
        }

        // Не нашли слово, а квиз еще не сформирован
        if (getStory().getQuizWords().isEmpty()) {
            UserContext.getUser().clearStories();

            // Слов нет, квиз недоступен
            if (UserContext.getUser().getCurrentDictionary().getWords().isEmpty()) {
                senderService.updateMessage(Text.QUIZ_CANNOT_BE_STARTED_NO_WORDS_AND_EMPTY, showDictionaryKeyboard());
            } else {
                senderService.updateMessage(Text.QUIZ_CANNOT_BE_STARTED_NO_WORDS, showDictionaryKeyboard());
            }
        } else {
            // Слова есть, начинаем полный или сокращенный квиз
            startQuiz();
        }
    }

    protected abstract void setAvailableQuizWords();

    private void startQuiz() {
        getStory().setQuizReady(true);
        Collections.shuffle(UserContext.getUser().getQuizStory().getQuizWords());
        getStory().setCurrentQuizWords(new ArrayList<>(getStory().getQuizWords()));
        quizCommandProcessor.processRequest();
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }

    private InlineKeyboardMarkup wordActionKeyboard(UserWord userWord) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.QUIZ_ADD_WORD_TO_QUIZ))
                                        .callbackData(WordActionCallback.builder()
                                                .type(WordAction.ADD_WORD_TO_QUIZ.getValue())
                                                .wordId(userWord.getWord().getId())
                                                .build().code())
                                        .build()
                        )
                )
                .keyboardRow(
                        Arrays.asList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.QUIZ_SKIP_WORD))
                                        .callbackData(WordActionCallback.builder()
                                                .type(WordAction.SKIP_WORD.getValue())
                                                .wordId(userWord.getWord().getId())
                                                .build().code())
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.QUIZ_DELETE_WORD_FROM_DICTIONARY))
                                        .callbackData(QuizDeleteWordCallback.builder()
                                                .wordId(userWord.getWord().getId())
                                                .dictionaryId(UserContext.getUser().getCurrentDictionary().getName())
                                                .build()
                                                .code()
                                        )
                                        .build()
                        )
                )
                .build();
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
}
