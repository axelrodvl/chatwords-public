package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.command.message.quiz.QuizNotificationsCommandProcessor;
import co.axelrod.chatwords.bot.formatter.DictionaryFormatter;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.oxford.Level;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.model.ServiceResponse;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.AlterDictionaryStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
public class AlterDictionaryCommandProcessor extends CommandProcessor<AlterDictionaryStory> {
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;
    private final CreateDictionaryCommandProcessor createDictionaryCommandProcessor;
    private final QuizNotificationsCommandProcessor quizNotificationsCommandProcessor;

    public AlterDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor,
            CreateDictionaryCommandProcessor createDictionaryCommandProcessor,
            QuizNotificationsCommandProcessor quizNotificationsCommandProcessor) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
        this.createDictionaryCommandProcessor = createDictionaryCommandProcessor;
        this.quizNotificationsCommandProcessor = quizNotificationsCommandProcessor;
    }

    @Override
    protected void processRequest() {
        AlterDictionaryStory story = getStory();

        if (story == null) {
            UserContext.getUser().setAlterDictionaryStory(new AlterDictionaryStory());
            analytics.log(UserEvent.ALTERING_DICTIONARY);
            senderService.updateMessage(
                    UserContext.getText(Text.DICTIONARY)
                            + " "
                            + DictionaryFormatter.currentDictionaryName(),
                    alterDictionaryKeyboard());
            return;
        }

        // Подключение и отключение нотификаций
        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
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

        if (story.getRenamingDictionary() == null && story.getDeletingDictionary() == null
                && !UserContext.getRequest().equals(UserContext.getText(Text.RENAME_DICTIONARY))
                && !UserContext.getRequest().equals(UserContext.getText(Text.DELETE_DICTIONARY))
                && !UserContext.getRequest().equals(UserContext.getText(Text.CANCEL))
                && !UserContext.getRequest().equals(Callback.TURN_ON_NOTIFICATIONS.getValue())
                && !UserContext.getRequest().equals(Callback.TURN_OFF_NOTIFICATIONS.getValue())) {
            senderService.updateMessage(
                    UserContext.getText(Text.DICTIONARY)
                            + " "
                            + DictionaryFormatter.currentDictionaryName(),
                    alterDictionaryKeyboard());
            return;
        }

        if (UserContext.getRequest().equals(UserContext.getText(Text.CANCEL))) {
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        if (UserContext.getRequest().equals(UserContext.getText(Text.RENAME_DICTIONARY))) {
            UserContext.getUser().getAlterDictionaryStory().setRenamingDictionary(true);
            senderService.updateMessage(Text.SET_DICTIONARY_NAME, null);
            return;
        }

        // Переименование словаря (ввод имени)
        if (story.getRenamingDictionary() != null) {
            ServiceResponse serviceResponse = userService.renameCurrentDictionary(UserContext.getRequest());
            if (!serviceResponse.isSuccess()) {
                analytics.log(serviceResponse.getUserEvent());
                senderService.updateMessage(serviceResponse.getText());
                return;
            }

            UserContext.getUser().clearStories();
            analytics.log(UserEvent.RENAMED_DICTIONARY);
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        if (UserContext.getRequest().equals(UserContext.getText(Text.DELETE_DICTIONARY))) {
            UserContext.getUser().getAlterDictionaryStory().setDeletingDictionary(true);

            senderService.updateMessage(
                    UserContext.getText(Text.YOU_ARE_GOING_TO_DELETE_DICTIONARY) + " <b>" + UserContext.getUser().getCurrentDictionary().getName() + "</b>" +
                            "\n" + UserContext.getText(Text.ARE_YOU_SURE),
                    deleteConfirmationKeyboard()
            );
            return;
        }

        if (Boolean.TRUE.equals(story.getDeletingDictionary())) {
            if (UserContext.getRequest().equals(UserContext.getText(Text.YES_DELETE_DICTIONARY))) {
                if (UserContext.getUser().getDictionaries().size() == 1) {
                    userService.deleteCurrentDictionary();

                    UserContext.getUser().clearStories();
                    analytics.log(UserEvent.REMOVED_DICTIONARY);
                    createDictionaryCommandProcessor.processRequest();
                } else {
                    if (UserContext.getUser().getCurrentDictionary().getName().equals(UserService.THOUSAND_DICTIONARY_NAME)) {
                        UserContext.getUser().setThousandWordsAdded(null);
                    }
                    if (UserContext.getUser().getCurrentDictionary().getName().equals(Level.getDictionaryName(Level.A1))
                            || UserContext.getUser().getCurrentDictionary().getName().equals(Level.getDictionaryName(Level.A2))
                            || UserContext.getUser().getCurrentDictionary().getName().equals(Level.getDictionaryName(Level.B1))
                            || UserContext.getUser().getCurrentDictionary().getName().equals(Level.getDictionaryName(Level.B2))
                    ) {
                        UserContext.getUser().setOxford3000Added(null);
                        UserContext.getUser().setLevel(null);
                    }

                    userService.deleteCurrentDictionary();

                    UserContext.getUser().clearStories();
                    analytics.log(UserEvent.REMOVED_DICTIONARY);
                    showDictionaryCommandProcessor.processRequest();
                }
            } else {
                UserContext.getUser().clearStories();
                showDictionaryCommandProcessor.processRequest();
            }
        }
    }

    @Override
    protected AlterDictionaryStory getStory() {
        return UserContext.getUser().getAlterDictionaryStory();
    }

    private InlineKeyboardMarkup alterDictionaryKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        if (UserContext.getUser().getCurrentDictionary().getNotificationsOn() == null
                || !UserContext.getUser().getCurrentDictionary().getNotificationsOn()
        ) {
            builder.keyboardRow(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(UserContext.getText(Text.TURN_ON_NOTIFICATIONS))
                            .callbackData(Callback.TURN_ON_NOTIFICATIONS.getValue())
                            .build()
            ));
        } else {
            builder.keyboardRow(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(UserContext.getText(Text.TURN_OFF_NOTIFICATIONS))
                            .callbackData(Callback.TURN_OFF_NOTIFICATIONS.getValue())
                            .build()
            ));
        }

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.RENAME_DICTIONARY))
                        .callbackData(UserContext.getText(Text.RENAME_DICTIONARY))
                        .build()
        ));


        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.DELETE_DICTIONARY))
                        .callbackData(UserContext.getText(Text.DELETE_DICTIONARY))
                        .build()
        ));

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.CANCEL))
                                .callbackData(UserContext.getText(Text.CANCEL))
                                .build()
                ))
                .build();

        return builder.build();
    }

    private InlineKeyboardMarkup deleteConfirmationKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.YES_DELETE_DICTIONARY))
                                .callbackData(UserContext.getText(Text.YES_DELETE_DICTIONARY))
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.CANCEL))
                                .callbackData(UserContext.getText(Text.CANCEL))
                                .build()
                ))
                .build();
    }
}
