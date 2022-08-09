package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.SetNotificationTimeCallback;
import co.axelrod.chatwords.bot.model.callback.SetNotificationTimeZoneCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.QuizStory;
import co.axelrod.chatwords.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import us.dustinj.timezonemap.TimeZoneMap;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;

@Component
@Slf4j
public class QuizNotificationsCommandProcessor extends CommandProcessor<QuizStory> {
    private final TimeZoneMap timeZoneMap = TimeZoneMap.forEverywhere();

    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public QuizNotificationsCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
    }

    @Override
    public void processRequest() {
        // Выключаем нотификации
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.TURN_OFF_NOTIFICATIONS.getValue())) {
            UserContext.getUser().getCurrentDictionary().setNotificationsOn(false);
            UserContext.getUser().getDictionaries().get(UserContext.getUser().getCurrentDictionary().getName()).setNotificationsOn(false);
            analytics.log(UserEvent.USER_TURNED_OFF_NOTIFICATIONS);
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        // Включаем нотификации, выбираем время
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.TURN_ON_NOTIFICATIONS.getValue())) {
            analytics.log(UserEvent.USER_WANT_TO_TURN_ON_NOTIFICATIONS);
            senderService.updateMessage(Text.CHOOSE_NOTIFICATIONS_TIME, chooseNotificationTimeKeyboard());
            return;
        }

        // Время выбрано, выбираем таймзону вручную
        if (UserContext.getRequest() != null && UserContext.getRequest().equals(Callback.SET_NOTIFICATION_TIME_ZONE_MANUALLY.getValue())) {
            senderService.updateMessage(Text.CHOOSE_NOTIFICATIONS_TIME_ZONE_BY_CITY, chooseTimeZoneManuallyKeyboard());
            return;
        }

        // Время выбрано, выбираем таймзону (отправка локации или вручную)
        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.equals(Callback.SET_NOTIFICATION_TIME)) {
            SetNotificationTimeCallback setNotificationTimeCallback = new SetNotificationTimeCallback(UserContext.getRequest());
            UserContext.getUser().getCurrentDictionary().setNotificationsOn(true);
            UserContext.getUser().getCurrentDictionary().setNotificationsTime(setNotificationTimeCallback.getTime());
            UserContext.getUser().getDictionaries().get(UserContext.getUser().getCurrentDictionary().getName()).setNotificationsOn(true);
            UserContext.getUser().getDictionaries().get(UserContext.getUser().getCurrentDictionary().getName()).setNotificationsTime(setNotificationTimeCallback.getTime());
            senderService.updateMessage(Text.CHOOSE_NOTIFICATIONS_TIME_ZONE, chooseTimeZoneKeyboard());
            return;
        }

        // Часовой пояс выбран
        if (callback != null && callback.equals(Callback.SET_NOTIFICATION_TIME_ZONE)) {
            SetNotificationTimeZoneCallback setNotificationTimeCallback = new SetNotificationTimeZoneCallback(UserContext.getRequest());
            UserContext.getUser().setTimeZoneId(setNotificationTimeCallback.getZone());
            UserContext.getUser().clearStories();
            analytics.log(UserEvent.USER_TURNED_ON_NOTIFICATIONS);
            senderService.updateMessage(
                    UserContext.getText(Text.YOUR_TIME_ZONE) + "\n" + UserContext.getUser().getTimeZoneId()
                            + "\n\n" + UserContext.getText(Text.YOU_WILL_BE_NOTIFIED_AT) + " " + UserContext.getUser().getCurrentDictionary().getNotificationsTime() + ":00",
                    notificationsSetKeyboard());
            return;
        }
        // Часовой пояс выбран отправкой локации
        if (UserContext.getLocation() != null) {
            log.info(UserContext.getLocation().toString());
            String zoneId = timeZoneMap.getOverlappingTimeZone(
                    UserContext.getLocation().getLatitude(),
                    UserContext.getLocation().getLongitude(
                    )).getZoneId();
            log.info("User timezone: " + zoneId);
            UserContext.getUser().setTimeZoneId(zoneId);
            UserContext.getUser().clearStories();
            analytics.log(UserEvent.USER_TURNED_ON_NOTIFICATIONS);
            senderService.updateMessage(
                    UserContext.getText(Text.YOUR_TIME_ZONE) + "\n" + UserContext.getUser().getTimeZoneId()
                            + "\n\n" + UserContext.getText(Text.YOU_WILL_BE_NOTIFIED_AT) + " " + UserContext.getUser().getCurrentDictionary().getNotificationsTime() + ":00",
                    notificationsSetKeyboard());
            return;
        }
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }

    protected InlineKeyboardMarkup notificationsSetKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.SHOW_DICTIONARY))
                        .callbackData(Callback.SHOW_DICTIONARY.getValue())
                        .build()
        ));

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.MENU_BUTTON))
                        .callbackData(UserContext.getText(Text.EXIT))
                        .build()
        ));

        return builder.build();
    }

    public InlineKeyboardMarkup chooseTimeZoneKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CHOOSE_NOTIFICATION_TIME_ZONE_MANUALLY))
                        .callbackData(Callback.SET_NOTIFICATION_TIME_ZONE_MANUALLY.getValue())
                        .build()
        ));

        return builder.build();
    }

    public InlineKeyboardMarkup chooseTimeZoneManuallyKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        int position = Text.TIME_ZONE_0.ordinal();
        for (int i = 0; i < 13; i++) {
            builder.keyboardRow(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(UserContext.getText(Text.values()[position + i]))
                            .callbackData(SetNotificationTimeZoneCallback.builder()
                                    .zone(ZoneId.of("+" + i).toString())
                                    .build().code())
                            .build()
            ));
        }

        return builder.build();
    }

    public InlineKeyboardMarkup chooseNotificationTimeKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        LocalTime localTime = LocalTime.now();
        localTime = LocalTime.of(localTime.getHour(), localTime.getMinute());
        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_CURRENT_TIME))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(localTime.toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_7))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(7, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_8))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(8, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_9))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(9, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_10))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(10, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_11))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(11, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_12))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(12, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_13))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(13, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_14))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(14, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_15))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(15, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_16))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(16, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_17))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(17, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_18))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(18, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_19))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(19, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_20))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(20, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_21))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(21, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        builder.keyboardRow(Arrays.asList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_22))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(22, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_23))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(23, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build(),
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.CLOCK_00))
                        .callbackData(SetNotificationTimeCallback.builder()
                                .time(LocalTime.of(0, 0).toString())
                                .dictionaryIdHash(HashUtil.getHash(UserContext.getUser().getCurrentDictionary().getName()))
                                .build().code())
                        .build()
        ));

        return builder.build();
    }
}
