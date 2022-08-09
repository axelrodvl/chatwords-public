package co.axelrod.chatwords.bot.command;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.message.*;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDetailedDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.command.message.quiz.QuizCommandProcessor;
import co.axelrod.chatwords.bot.command.message.translation.ChangeWordTranslationCallbackCommandProcessor;
import co.axelrod.chatwords.bot.command.message.translation.DetailedWordTranslationCallbackCommandProcessor;
import co.axelrod.chatwords.bot.command.message.translation.SentenceToDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.command.message.translation.TranslateCommandProcessor;
import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.paging.PagingScreen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class RequestProcessor {
    private final BotConfigurationProperties botConfigurationProperties;
    private final Analytics analytics;
    private final UserRepository userRepository;

    public RequestProcessor(
            BotConfigurationProperties botConfigurationProperties,
            Analytics analytics,
            UserRepository userRepository
    ) {
        this.botConfigurationProperties = botConfigurationProperties;
        this.analytics = analytics;
        this.userRepository = userRepository;
    }

    @Autowired
    private StartCommandProcessor startCommandProcessor;
    @Autowired
    private TranslateCommandProcessor translateCommandProcessor;
    @Autowired
    private SentenceToDictionaryCommandProcessor sentenceToDictionaryCommandProcessor;
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;
    @Autowired
    private ChangeDictionaryCommandProcessor changeDictionaryCommandProcessor;
    @Autowired
    private ShowDictionaryCommandProcessor showDictionaryCommandProcessor;
    @Autowired
    private CreateDictionaryCommandProcessor createDictionaryCommandProcessor;
    @Autowired
    private AlterDictionaryCommandProcessor alterDictionaryCommandProcessor;
    @Autowired
    private ThousandWordsCommandProcessor thousandWordsCommandProcessor;
    @Autowired
    private Oxford3000CommandProcessor oxford3000CommandProcessor;
    @Autowired
    private MainMenuCommandProcessor mainMenuCommandProcessor;
    @Autowired
    private FeedbackCommandProcessor feedbackCommandProcessor;
    @Autowired
    private ShowDetailedDictionaryCommandProcessor showDetailedDictionaryCommandProcessor;
    @Autowired
    private RemoveWordCallbackCommandProcessor removeWordCallbackCommandProcessor;
    @Autowired
    private DetailedWordTranslationCallbackCommandProcessor detailedWordTranslationCallbackCommandProcessor;
    @Autowired
    private ChangeWordTranslationCallbackCommandProcessor changeWordTranslationCallbackCommandProcessor;
    @Autowired
    private UserSettingsCommandProcessor userSettingsCommandProcessor;
    @Autowired
    private TurnOffWordOfTheDayNotificationsCallbackCommandProcessor turnOffWordOfTheDayNotificationsCallbackCommandProcessor;
    @Autowired
    private MaintenanceModeProcessor maintenanceModeProcessor;

    @Async("threadPoolTaskExecutor")
    public void processUpdate(Update update) {
        Instant start = Instant.now();

        if (update.getMessage() != null) {
            if (update.getMessage().getText() == null
                    && update.getMessage().getCaption() == null
                    && update.getMessage().getLocation() == null) {
                return;
            }

            UserContext.initialize(update.getMessage(), userRepository);
            CommandProcessor commandProcessor = getCommandProcessor();

            if (Boolean.TRUE.equals(botConfigurationProperties.getMaintenanceMode())) {
                maintenanceModeProcessor.process();
            } else {
                commandProcessor.process();
            }

            UserContext.close();
        }
        if (update.getCallbackQuery() != null) {
            UserContext.initialize(update.getCallbackQuery(), userRepository);
            CommandProcessor commandProcessor = getCallbackProcessor();

            if (Boolean.TRUE.equals(botConfigurationProperties.getMaintenanceMode())) {
                maintenanceModeProcessor.process();
            } else {
                commandProcessor.process();
            }

            UserContext.close();
        }
        if (update.getMyChatMember() != null) {
            UserContext.initialize(update.getMyChatMember(), userRepository);
            String oldStatus = update.getMyChatMember().getOldChatMember().getStatus();
            String newStatus = update.getMyChatMember().getNewChatMember().getStatus();
            if (oldStatus.equals("member") && newStatus.equals("kicked")) {
                analytics.log(UserContext.getUser(), UserEvent.USER_HAS_DELETED_BOT);
                UserContext.getUser().setBotDeleted(true);
            }
            if (oldStatus.equals("kicked") && newStatus.equals("member")) {
                analytics.log(UserContext.getUser(), UserEvent.USER_HAS_RESTARTED_BOT_AFTER_DELETING);
                UserContext.getUser().setBotDeleted(false);
            }
            userRepository.save(UserContext.getUser());
            UserContext.close();
        }

        Instant end = Instant.now();
        log.info("Request processed in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
    }

    public CommandProcessor getCommandProcessor() {
        if (UserContext.getLocation() != null && UserContext.getUser().getQuizStory() != null) {
            return quizCommandProcessor;
        }

        if (UserContext.getRequest().startsWith("/")) {
            if (UserContext.getRequest().contains("/start") && UserContext.getRequest().split(" ").length > 1) {
                return startCommandProcessor;
            }

            switch (Command.getCommand(UserContext.getRequest())) {
                case START:
                    return startCommandProcessor;

                case NEW_DICTIONARY:
                    return createDictionaryCommandProcessor;
                case CHANGE_DICTIONARY:
                    return changeDictionaryCommandProcessor;
                case MY_1000:
                    return thousandWordsCommandProcessor;
                case OXFORD_3000:
                    return oxford3000CommandProcessor;
                case FEEDBACK:
                    return feedbackCommandProcessor;
                case SETTINGS:
                    return userSettingsCommandProcessor;

                case HOME:
                case UNKNOWN:
                    return mainMenuCommandProcessor;
            }
        }

        if (UserContext.getUser().getStartStory() != null) {
            return startCommandProcessor;
        }
        if (UserContext.getUser().getQuizStory() != null) {
            return quizCommandProcessor;
        }
        if (UserContext.getUser().getCreateDictionaryStory() != null) {
            return createDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getChangeDictionaryStory() != null) {
            return changeDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getAlterDictionaryStory() != null) {
            return alterDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getThousandWordsDictionaryStory() != null) {
            return thousandWordsCommandProcessor;
        }
        if (UserContext.getUser().getOxford3000DictionaryStory() != null) {
            return oxford3000CommandProcessor;
        }
        if (UserContext.getUser().getFeedbackStory() != null) {
            return feedbackCommandProcessor;
        }
        if (UserContext.getUser().getUserSettingsStory() != null) {
            return userSettingsCommandProcessor;
        }
        if (UserContext.getUser().getSentenceToDictionaryStory() != null) {
            return sentenceToDictionaryCommandProcessor;
        }

        return translateCommandProcessor;
    }

    public CommandProcessor getCallbackProcessor() {
        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());

        switch (Command.getCommand(UserContext.getRequest())) {
            case START:
                return startCommandProcessor;

            case NEW_DICTIONARY:
                return createDictionaryCommandProcessor;
            case CHANGE_DICTIONARY:
                return changeDictionaryCommandProcessor;
            case MY_1000:
                return thousandWordsCommandProcessor;
            case OXFORD_3000:
                return oxford3000CommandProcessor;
            case FEEDBACK:
                return feedbackCommandProcessor;
            case SETTINGS:
                return userSettingsCommandProcessor;

            case HOME:
                return mainMenuCommandProcessor;
        }

        if (UserContext.getRequest().equals(Command.HOME.getValue())) {
            return mainMenuCommandProcessor;
        }

        if (UserContext.getRequest().equals(StartCommandProcessor.FIRST_WORD)) {
            return translateCommandProcessor;
        }

        if (callback != null && callback.equals(Callback.REMOVE_WORD)) {
            return removeWordCallbackCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.DETAILED_WORD_TRANSLATION)) {
            return detailedWordTranslationCallbackCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.CHANGE_WORD_TRANSLATION_VARIANT)) {
            return changeWordTranslationCallbackCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.CHANGE_TRANSLATION_LANGUAGE)) {
            return translateCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.CHANGE_TRANSLATION_MODE)) {
            return translateCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.ADD_WORD_TO_DICTIONARY)) {
            return translateCommandProcessor;
        }
        if (callback != null && callback.equals(Callback.TURN_OFF_WORD_OF_THE_DAY_NOTIFICATIONS)) {
            return turnOffWordOfTheDayNotificationsCallbackCommandProcessor;
        }

        if (UserContext.getUser().getStartStory() != null) {
            return startCommandProcessor;
        }
        if (UserContext.getUser().getQuizStory() != null) {
            return quizCommandProcessor;
        }
        if (UserContext.getUser().getCreateDictionaryStory() != null) {
            return createDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getChangeDictionaryStory() != null) {
            return changeDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getAlterDictionaryStory() != null) {
            return alterDictionaryCommandProcessor;
        }
        if (UserContext.getUser().getThousandWordsDictionaryStory() != null) {
            return thousandWordsCommandProcessor;
        }
        if (UserContext.getUser().getOxford3000DictionaryStory() != null) {
            return oxford3000CommandProcessor;
        }
        if (UserContext.getUser().getFeedbackStory() != null) {
            return feedbackCommandProcessor;
        }
        if (UserContext.getUser().getUserSettingsStory() != null) {
            return userSettingsCommandProcessor;
        }
        if (UserContext.getUser().getSentenceToDictionaryStory() != null) {
            return sentenceToDictionaryCommandProcessor;
        }

        callback = Callback.getCallback(UserContext.getRequest());

        if (callback != null && (callback.equals(Callback.NEXT_PAGE) || callback.equals(Callback.PREVIOUS_PAGE))) {
            PagingScreen pagingScreen = UserContext.getUser().getPaging().getPagingScreen();

            if (pagingScreen == PagingScreen.DICTIONARY) {
                return showDictionaryCommandProcessor;
            } else if (pagingScreen == PagingScreen.DETAILED_DICTIONARY) {
                return showDetailedDictionaryCommandProcessor;
            }
        }

        switch (callback) {
            case REMOVE_WORD:
                return removeWordCallbackCommandProcessor;

            case DETAILED_DICTIONARY:
            case CHANGE_DIRECTION_DETAILED:
            case PREVIOUS_PAGE:
            case NEXT_PAGE:
                return showDetailedDictionaryCommandProcessor;

            case ALTER_DICTIONARY:
                return alterDictionaryCommandProcessor;

            case QUIZ:
            case SKIP_WORDS_LEARNING:
            case CONTINUE_QUIZ:
                return quizCommandProcessor;

            case CHANGE_DICTIONARY:
                return changeDictionaryCommandProcessor;

            case SET_QUIZ_LENGTH:
                return userSettingsCommandProcessor;

            case SHOW_DICTIONARY:
            case CHANGE_DIRECTION:
            case SHOW_LEARNED:
            case HIDE_LEARNED:
            case CHANGE_SORTING:
                return showDictionaryCommandProcessor;

            case SENTENCE_TO_DICTIONARY:
                return sentenceToDictionaryCommandProcessor;

            case UNKNOWN:
            default:
                return mainMenuCommandProcessor;
        }
    }
}
