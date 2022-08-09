package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.Command;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.formatter.ActivityFormatter;
import co.axelrod.chatwords.bot.formatter.UserProgressFormatter;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.NoStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;

@Component
public class MainMenuCommandProcessor extends CommandProcessor<NoStory> {
    private static final int HINT_COUNT = 7;

    public MainMenuCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
    }

    @Override
    public void processRequest() {
        UserContext.setIsCaption(false);
        UserContext.getUser().clearStories();
        analytics.log(UserEvent.MAIN_MENU);
        UserContext.setCustomCallbackAlertText(UserContext.getText(Text.MAIN_MENU_ALERT));
        senderService.updateMessage(
                UserProgressFormatter.format(userService.getUserProgress())
                        + "\n\n"
                        + ActivityFormatter.getFormattedActivity(UserContext.getUser().getCurrentDayActivity())
                        + "\n\n"
                        + getNextHint()
                ,
                mainMenuKeyboard());
    }

    private String getNextHint() {
        if (UserContext.getUser().getCurrentHint() == null) {
            UserContext.getUser().setCurrentHint(1);
        } else {
            if (UserContext.getUser().getCurrentHint() == HINT_COUNT) {
                UserContext.getUser().setCurrentHint(1);
            } else {
                UserContext.getUser().setCurrentHint(1 + UserContext.getUser().getCurrentHint());
            }
        }

        // Fallback
        String hint = UserContext.getText(Text.HINT_1);
        int currentHint = UserContext.getUser().getCurrentHint();

        switch (currentHint) {
            case 1:
                hint = UserContext.getText(Text.HINT_1);
                break;
            case 2:
                hint = UserContext.getText(Text.HINT_2);
                break;
            case 3:
                hint = UserContext.getText(Text.HINT_3);
                break;
            case 4:
                hint = UserContext.getText(Text.HINT_4);
                break;
            case 5:
                hint = UserContext.getText(Text.HINT_5);
                break;
            case 6:
                hint = UserContext.getText(Text.HINT_6);
                break;
            case 7:
                hint = UserContext.getText(Text.HINT_7);
                break;
        }

//        if (currentHint != 1) {
//            analytics.log(UserEvent.SHOW_AD);
//        }

        return hint;
    }

    private InlineKeyboardMarkup mainMenuKeyboard() {
        /**
         * Выбрать словарь
         * Создать словарь
         * Отзыв / Настройки
         * Квиз
         * Текущий словарь
         */

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_MY_1000))
                        .callbackData(Command.MY_1000.getValue())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.OXFORD_3000))
                        .callbackData(Command.OXFORD_3000.getValue())
                        .build()
        ));

        builder.keyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.LEAVE_REVIEW))
                                .callbackData(Command.FEEDBACK.getValue())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_SETTINGS))
                                .callbackData(Command.SETTINGS.getValue())
                                .build()
                )
        );

//        if (!UserContext.getUser().getCurrentDictionary().getWords().isEmpty()) {
//            builder.keyboardRow(
//                    Collections.singletonList(
//                            InlineKeyboardButton.builder()
//                                    .text(UserContext.getText(Text.START_QUIZ))
//                                    .callbackData(Callback.QUIZ.getValue())
//                                    .build()
//                    )
//            );
//        }

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_NEW_DICTIONARY))
                        .callbackData(Command.NEW_DICTIONARY.getValue())
                        .build()
        ));

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_SET_DICTIONARY))
                        .callbackData(Command.CHANGE_DICTIONARY.getValue())
                        .build()
        ));

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_CURRENT_DICTIONARY))
                        .callbackData(Callback.SHOW_DICTIONARY.getValue())
                        .build()
        ));

        return builder.build();
    }

    @Override
    protected NoStory getStory() {
        return null;
    }
}
