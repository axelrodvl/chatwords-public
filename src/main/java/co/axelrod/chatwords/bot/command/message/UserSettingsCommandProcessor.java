package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.LanguageCodeCallback;
import co.axelrod.chatwords.bot.model.callback.MyLanguageCodeCallback;
import co.axelrod.chatwords.bot.model.callback.SetQuizLengthCallback;
import co.axelrod.chatwords.bot.model.callback.TurnOffAllNotificationsCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.UserSettingsStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;

@Component
public class UserSettingsCommandProcessor extends CommandProcessor<UserSettingsStory> {
    public static final int QUIZ_LENGTH_SHORT = 5;
    public static final int QUIZ_LENGTH_MEDIUM = 10;
    public static final int QUIZ_LENGTH_LONG = 20;

    private final MainMenuCommandProcessor mainMenuCommandProcessor;

    public UserSettingsCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            MainMenuCommandProcessor mainMenuCommandProcessor) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.mainMenuCommandProcessor = mainMenuCommandProcessor;
    }

    @Override
    protected void processRequest() {
        UserSettingsStory story = getStory();

        if (story == null) {
            UserContext.getUser().setUserSettingsStory(new UserSettingsStory());
            analytics.log(UserEvent.USER_SETTINGS);
            senderService.updateMessage(Text.USER_SETTINGS, settingsKeyboard());
        } else {
            if (UserContext.getRequest().equals(UserContext.getText(Text.EXIT))) {
                UserContext.getUser().clearStories();
                mainMenuCommandProcessor.process();
                return;
            }

            if (UserContext.getRequest().equals(Callback.SET_QUIZ_LENGTH.getValue())) {
                analytics.log(UserEvent.SETTING_QUIZ_LENGTH);
                senderService.updateMessage(Text.CHOOSE_QUIZ_LENGTH, quizLengthKeyboard());
            }

            if (UserContext.getRequest().equals(Callback.MY_LANGUAGE.getValue())) {
                analytics.log(UserEvent.SETTING_USER_FROM_LANGUAGE);
                senderService.updateMessage(Text.SET_YOUR_LANGUAGE, chooseMyLanguageKeyboard());
            }

            if (UserContext.getRequest().equals(Callback.LANGUAGE_CODE.getValue())) {
                analytics.log(UserEvent.SETTING_INTERFACE_LANGUAGE);
                senderService.updateMessage(Text.CHOOSE_INTERFACE_LANGUAGE, chooseInterfaceLanguageKeyboard());
            }

            if (CallbackUtil.getCallback(UserContext.getRequest()) != null) {
                if (CallbackUtil.getCallback(UserContext.getRequest()).getId().equals(Callback.SET_QUIZ_LENGTH.getId())) {
                    SetQuizLengthCallback setQuizLengthCallback = new SetQuizLengthCallback(UserContext.getRequest());
                    if (setQuizLengthCallback.getLength() != null) {
                        userService.setQuizLength(Integer.parseInt(setQuizLengthCallback.getLength()));
                        UserContext.getUser().clearStories();
                        analytics.log(UserEvent.getQuizLength(Integer.valueOf(setQuizLengthCallback.getLength())));
                        mainMenuCommandProcessor.process();
                    }
                }

                if (CallbackUtil.getCallback(UserContext.getRequest()).getId().equals(Callback.MY_LANGUAGE.getId())) {
                    MyLanguageCodeCallback myLanguageCodeCallback = new MyLanguageCodeCallback(UserContext.getRequest());
                    if (myLanguageCodeCallback.getLanguageCode() != null) {
                        userService.setFromLanguage(Language.getLanguageById(myLanguageCodeCallback.getLanguageCode()));
                        UserContext.getUser().clearStories();
                        analytics.log(UserEvent.getFromLanguage(UserContext.getUser().getDefaultFromLanguage()));
                        mainMenuCommandProcessor.process();
                    }
                }

                if (CallbackUtil.getCallback(UserContext.getRequest()).getId().equals(Callback.LANGUAGE_CODE.getId())) {
                    LanguageCodeCallback languageCodeCallback = new LanguageCodeCallback(UserContext.getRequest());
                    if (languageCodeCallback.getLanguageCode() != null) {
                        userService.setLanguageCode(languageCodeCallback.getLanguageCode());
                        UserContext.getUser().clearStories();
                        analytics.log(UserEvent.getInterfaceLanguage(languageCodeCallback.getLanguageCode()));
                        UserContext.setLanguageCode(languageCodeCallback.getLanguageCode());
                        mainMenuCommandProcessor.process();
                    }
                }

                if (CallbackUtil.getCallback(UserContext.getRequest()).getId().equals(Callback.TURN_OFF_ALL_NOTIFICATIONS.getId())) {
                    userService.turnOffAllNotifications();

                    UserContext.getUser().clearStories();
                    analytics.log(UserEvent.USER_TURNED_OFF_ALL_NOTIFICATIONS);

                    senderService.updateMessage(Text.ALL_NOTIFICATIONS_TURNED_OFF, null);
                    UserContext.setQueryId(null);
                    mainMenuCommandProcessor.process();
                }
            }
        }
    }

    @Override
    protected UserSettingsStory getStory() {
        return UserContext.getUser().getUserSettingsStory();
    }

    private InlineKeyboardMarkup settingsKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.CHANGE_MY_LANGUAGE))
                                .callbackData(Callback.MY_LANGUAGE.getValue())
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.INTERFACE_LANGUAGE))
                                .callbackData(Callback.LANGUAGE_CODE.getValue())
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.QUIZ_LENGTH))
                                .callbackData(Callback.SET_QUIZ_LENGTH.getValue())
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.TURN_OFF_ALL_NOTIFICATIONS))
                                .callbackData(TurnOffAllNotificationsCallback.builder()
                                        .build()
                                        .code()
                                )
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_BUTTON))
                                .callbackData(UserContext.getText(Text.EXIT))
                                .build()
                ))
                .build();
    }

    private InlineKeyboardMarkup quizLengthKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(Integer.toString(QUIZ_LENGTH_SHORT))
                                .callbackData(SetQuizLengthCallback.builder()
                                        .length(Integer.toString(QUIZ_LENGTH_SHORT))
                                        .build()
                                        .code()
                                )
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(Integer.toString(QUIZ_LENGTH_MEDIUM))
                                .callbackData(SetQuizLengthCallback.builder()
                                        .length(Integer.toString(QUIZ_LENGTH_MEDIUM))
                                        .build()
                                        .code()
                                )
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(Integer.toString(QUIZ_LENGTH_LONG))
                                .callbackData(SetQuizLengthCallback.builder()
                                        .length(Integer.toString(QUIZ_LENGTH_LONG))
                                        .build()
                                        .code()
                                )
                                .build()
                ))
                .build();
    }

    private InlineKeyboardMarkup chooseInterfaceLanguageKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        Arrays.stream(Language.values())
                .filter(Language::isUserInterface)
                .forEach(language -> builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                                .callbackData(LanguageCodeCallback.builder()
                                        .languageCode(language.getValue())
                                        .build()
                                        .code())
                                .build()
                )));

        return builder.build();
    }

    private InlineKeyboardMarkup chooseMyLanguageKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        Arrays.stream(Language.values())
                .filter(Language::isUserInterface)
                .forEach(language -> builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                                .callbackData(MyLanguageCodeCallback.builder()
                                        .languageCode(language.getValue())
                                        .build()
                                        .code())
                                .build()
                )));

        return builder.build();
    }
}