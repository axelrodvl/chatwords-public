package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.MainMenuCommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.service.quiz.model.QuizMode;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.QuizStory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Component
@Slf4j
public class QuizCommandProcessor extends CommandProcessor<QuizStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;
    private final QuizService quizService;

    @Autowired
    private QuizTutorialProcessor quizTutorialProcessor;

    @Autowired
    private QuizSetQuizTypeProcessor quizSetQuizTypeProcessor;

    @Autowired
    private QuizNewWordsChooseWordsProcessor quizNewWordsChooseWordsProcessor;

    @Autowired
    private QuizRepeatWordsChooseWordsProcessor quizRepeatWordsChooseWordsProcessor;

    @Autowired
    private QuizSourceToTargetProcessor quizSourceToTargetProcessor;

    @Autowired
    private QuizWordBuilderProcessor quizWordBuilderProcessor;

    @Autowired
    private QuizTargetToSourceProcessor quizTargetToSourceProcessor;

    @Autowired
    private QuizNotificationsCommandProcessor quizNotificationsCommandProcessor;

    public QuizCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            MainMenuCommandProcessor mainMenuCommandProcessor,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor,
            QuizService quizService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.mainMenuCommandProcessor = mainMenuCommandProcessor;
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
        this.quizService = quizService;
    }

    @Override
    protected void processRequest() {
        // Словарь пустой, квиз недоступен
        if (quizService.quizUnavailable()) {
            analytics.log(UserEvent.QUIZ_UNAVAILABLE);
            senderService.sendMessage(Text.ADD_WORDS_TO_REPEAT);
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        // Показываем туториал
        if (UserContext.getUser().getOnboarding() == null || UserContext.getUser().getOnboarding().isShowQuizTutorial()) {
            analytics.log(UserEvent.SHOWING_QUIZ_TUTORIAL);
            quizTutorialProcessor.processRequest();
            return;
        }

        // Пользователь продолжает квиз
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.CONTINUE_QUIZ.getValue())) {
            getStory().setQuizCompleted(false);
            getStory().setQuizMode(QuizMode.TARGET_TYPING);
            getStory().setCurrentQuizWords(new ArrayList<>(getStory().getQuizWords()));
        }

        // Возврат к словарю
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.SHOW_DICTIONARY.getValue())) {
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        // Подключение и отключение нотификаций
        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.getId().equals(Callback.CANCEL.getId())) {
            UserContext.getUser().clearStories();
            mainMenuCommandProcessor.process();
            return;
        }
        if (callback != null && callback.equals(Callback.SET_NOTIFICATION_TIME)) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }
        if (callback != null && callback.equals(Callback.SET_NOTIFICATION_TIME_ZONE)) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }
        if (UserContext.getLocation() != null) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.TURN_ON_NOTIFICATIONS.getValue())) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.TURN_OFF_NOTIFICATIONS.getValue())) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.SET_NOTIFICATION_TIME_ZONE_MANUALLY.getValue())) {
            quizNotificationsCommandProcessor.processRequest();
            return;
        }

        // Выбор типа квиза
        if (getStory().getQuizType() == null) {
            analytics.log(UserEvent.CHOOSING_QUIZ_TYPE);
            quizSetQuizTypeProcessor.processRequest();
            return;
        }

        // Наполнение квиза словами
        if (!getStory().getQuizReady()) {
            switch (getStory().getQuizType()) {
                // С добавлением слов из словаря
                case NEW: {
                    analytics.log(UserEvent.QUIZ_TYPE_NEW);
                    quizNewWordsChooseWordsProcessor.processRequest();
                    return;
                }
                // С повторение слов
                case REPEAT: {
                    analytics.log(UserEvent.QUIZ_TYPE_REPEAT);
                    quizRepeatWordsChooseWordsProcessor.processRequest();
                    return;
                }
                // Случайные слова
                case RANDOM: {
                    analytics.log(UserEvent.QUIZ_TYPE_RANDOM);
                    quizService.setRandomWordsForQuiz();
                    getStory().setQuizReady(true);
                    getStory().setCurrentQuizWords(new ArrayList<>(getStory().getQuizWords()));
                }
                break;
            }
        }

        // Если квиз уже завершен - отправляем результаты
        if (getStory().getQuizCompleted()) {
            // get quiz results
            // TODO Показать ошибочные слова?

            String response = UserContext.getText(Text.QUIZ_COMPLETED) + "\n" + UserContext.getText(Text.QUIZ_CORRECT_ANSWERS_COUNT) + " " + getCorrectWordsCount();
            if (getStory().getWordsCorrect().equals(quizService.getQuizLength())) {
                response += "\n" + UserContext.getText(Text.GOOD_JOB);
            }

            if (UserContext.getUser().getCurrentDictionary().getLearnedWordsCount().equals(UserContext.getUser().getCurrentDictionary().getWords().size())) {
                response += "\n" + UserContext.getText(Text.DICTIONARY_LEARNT);
            } else {
                response += "\n" + UserContext.getText(Text.DICTIONARY_LEARNT_FOR_PERCENT) + " " + UserContext.getUser().getCurrentDictionary().getLearnedPercent();
            }

            UserContext.getUser().getCurrentDayActivity().quizPassed();
            analytics.log(UserEvent.FINISHED_QUIZ);
            senderService.updateMessage(response, finishedQuizKeyboard());
            return;
        }

//        quizSourceToTargetProcessor.processRequest();
//        quizWordBuilderProcessor.processRequest();
//
//        // Устанавливаем следующий тип квиза, если предыдущий завершен
//
////        // Квиз еще не завершен, начинаем квиз
//
//        // Если квиз еще идет - просто перекидываем, иначезаполняем currentQuizWords
//
        switch (getStory().getQuizMode()) {
            case SOURCE_TO_TARGET:
                quizSourceToTargetProcessor.processRequest();
                break;
            case TARGET_TO_SOURCE:
                quizTargetToSourceProcessor.processRequest();
                break;
            case SOURCE_TYPING:
                quizWordBuilderProcessor.processRequest();
                break;
            case TARGET_TYPING:
                quizWordBuilderProcessor.processRequest();
                break;
        }
//        quizWordBuilderProcessor.processRequest();
//        quizSourceToTargetProcessor.processRequest();
    }


    public String getCorrectWordsCount() {
        // TODO Грязный фикс! Здесь должно быть количество квизов, которое пользователь прошел.
        //      Например, если прошел 2 - умножаем на 2, прошел 3 - умножаем на 3
        if (getStory().getQuizMode().equals(QuizMode.TARGET_TYPING)) {
            return getStory().getWordsCorrect() + "/" + getStory().getQuizWords().size() * 2;
        } else {
            return getStory().getWordsCorrect() + "/" + getStory().getQuizWords().size();
        }
    }

    @Override
    protected QuizStory getStory() {
        if (UserContext.getUser().getQuizStory() == null) {
            quizService.initializeQuiz();
        }
        return UserContext.getUser().getQuizStory();
    }

    protected InlineKeyboardMarkup finishedQuizKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        if (!getStory().getQuizMode().equals(QuizMode.TARGET_TYPING)) {
            builder.keyboardRow(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(UserContext.getText(Text.CONTINUE_QUIZ))
                            .callbackData(Callback.CONTINUE_QUIZ.getValue())
                            .build()
            ));
        }

        if (UserContext.getUser().getCurrentDictionary().getNotificationsOn() == null
                || !UserContext.getUser().getCurrentDictionary().getNotificationsOn()
        ) {
            builder.keyboardRow(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(UserContext.getText(Text.TURN_ON_NOTIFICATIONS))
                            .callbackData(Callback.TURN_ON_NOTIFICATIONS.getValue())
                            .build()
            ));
        }
//        else {
//            builder.keyboardRow(Collections.singletonList(
//                    InlineKeyboardButton.builder()
//                            .text(UserContext.getText(Text.TURN_OFF_NOTIFICATIONS))
//                            .callbackData(Callback.TURN_OFF_NOTIFICATIONS.getValue())
//                            .build()
//            ));
//        }

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.DICTIONARY_BUTTON))
                        .callbackData(Callback.SHOW_DICTIONARY.getValue())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_BUTTON))
                        .callbackData(CancelCallback.builder().build().code())
                        .build()
        ));

        return builder.build();
    }
}
