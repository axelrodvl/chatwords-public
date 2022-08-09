package co.axelrod.chatwords.bot.reminder;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.configuration.QuizConfigurationProperties;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.storage.User;
import co.axelrod.chatwords.storage.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizReminder {
    private final Analytics analytics;
    private final QuizConfigurationProperties quizConfigurationProperties;
    private final UserRepository userRepository;
    private final SenderService senderService;

    //    @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow") // Every minute
//    @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Moscow") // Every day at 12:00 MSK
    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Moscow") // Every hour
    public void remindUsersForQuiz() {
        log.info("Started hourly reminder for quiz");
        userRepository.findUsersForReminder().stream()
                .filter(user -> user.getBotDeleted() == null || Boolean.FALSE.equals(user.getBotDeleted()))
                .forEach(this::remindUserAboutQuiz);
        log.info("Finished hourly reminder for quiz");
    }

    private void remindUserAboutQuiz(User user) {
        user.getDictionaries().values().forEach(userDictionary -> {
            // TODO Одна нотификация на пользователя! Не по каждому словарю! (а надо?)
            if (userDictionary.getNotificationsOn() != null && userDictionary.getNotificationsOn()) {
                // Получаем время, в которое нужно отправить нотификацию
                ZoneId zoneId = ZoneId.of(user.getTimeZoneId());
                LocalTime notificationLocalTime = LocalTime.parse(userDictionary.getNotificationsTime());

                int hourOfNotification = notificationLocalTime.getHour();
                int currentHourAtTargetZone = ZonedDateTime.now(zoneId).getHour();

                boolean sendNotification = currentHourAtTargetZone == hourOfNotification;

                if (sendNotification) {
                    Locale locale = new Locale(user.getLanguageCode());
                    ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
                    String message = String.format(messages.getString(Text.TIME_FOR_QUIZ.getValue()), userDictionary.getName());

                    InlineKeyboardMarkup quizKeyboard = InlineKeyboardMarkup.builder()
                            .keyboard(Collections.singletonList(
                                    Collections.singletonList(InlineKeyboardButton.builder()
                                            .text(messages.getString(Text.START_QUIZ.getValue()))
                                            .callbackData(Callback.QUIZ.getValue())
                                            .build()
                                    )
                            ))
                            .keyboard(Collections.singletonList(
                                    Collections.singletonList(InlineKeyboardButton.builder()
                                            .text(messages.getString(Text.MENU_BUTTON.getValue()))
                                            .callbackData(CancelCallback.builder().build().code())
                                            .build()
                                    )
                            ))
                            .build();

                    analytics.log(user, UserEvent.QUIZ_REMINDER);
                    SendMessage sendMessage = SendMessage.builder()
                            .chatId(user.getId())
                            .text(message)
                            .replyMarkup(quizKeyboard)
                            .disableNotification(false)
                            .parseMode(ParseMode.HTML)
                            .build();
                    senderService.sendWithoutSession(sendMessage, user);
                    log.info("Quiz reminder for " + user.getName());
                }
            }
        });
    }
}
