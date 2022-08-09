package co.axelrod.chatwords.bot.reminder;

import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.formatter.model.WordKnowledge;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.model.callback.TurnOffWordOfTheDayNotificationsCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
@Slf4j
public class WordOfTheDayReminder {
    private static final int USERS_PER_ITERATION = 10;

    private final UserRepository userRepository;
    private final SenderService senderService;
    private final ChatWordsRepository chatWordsRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow") // Every day at 00:00 MSK
    public void resetWordOfTheDayOffset() {
        ChatWords chatWords = chatWordsRepository.getConfig();
        chatWords.getWordOfTheDay().reset();
        chatWords.getWordOfTheDay().setUsersToNotify(userRepository.totalUsersForWordOfTheDay().intValue());
        log.info("Users to notify: " + chatWords.getWordOfTheDay().getUsersToNotify());
        chatWordsRepository.save(chatWords);
    }

    //    @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow") // [TEST] Every minute
    @Scheduled(cron = "*/2 * 10-18 * * *", zone = "Europe/Moscow") // [PROD] Every 2 seconds, from 10 to 18 MSK
    public void remindUsersForQuiz() {
        // Fixing
        if (!chatWordsRepository.getConfig().getWordOfTheDay().getFinished()) {
            if (chatWordsRepository.getConfig().getWordOfTheDay().getUsersToNotify().equals(0)) {
                resetWordOfTheDayOffset();
            }

            log.info("Started reminder for word of the day (users: " +
                    chatWordsRepository.getConfig().getWordOfTheDay().getNotifiedUsers()
                    + "/"
                    + chatWordsRepository.getConfig().getWordOfTheDay().getUsersToNotify()
                    + ")");

            ChatWords chatWords = chatWordsRepository.getConfig();
            int page = chatWords.getWordOfTheDay().getNotifiedUsers() / USERS_PER_ITERATION;
            PageRequest request = PageRequest.of(page, USERS_PER_ITERATION, Sort.by(Sort.Direction.ASC, "id"));

            try {
                userRepository.findUsersForWordOfTheDay(request).getContent().stream()
                        .filter(user -> user.getBotDeleted() == null || Boolean.FALSE.equals(user.getBotDeleted()))
                        .forEach(this::remindForWordOfTheDay);
            } catch (Exception ex) {
                log.error("Unable to remind users: " +
                        chatWordsRepository.getConfig().getWordOfTheDay().getNotifiedUsers()
                        + "/"
                        + chatWordsRepository.getConfig().getWordOfTheDay().getUsersToNotify()
                        + ")");
            }

            if (chatWordsRepository.getConfig().getWordOfTheDay().getNotifiedUsers() >=
                    chatWordsRepository.getConfig().getWordOfTheDay().getUsersToNotify()
            ) {
                chatWords = chatWordsRepository.getConfig();
                chatWords.getWordOfTheDay().setFinished(true);
                chatWordsRepository.save(chatWords);
                log.info("Word of the day reminder completed, total users: " + chatWords.getWordOfTheDay().getNotifiedUsers());
            } else {
                chatWords = chatWordsRepository.getConfig();
                Integer notifiedUsers = chatWords.getWordOfTheDay().getNotifiedUsers();
                chatWords.getWordOfTheDay().setNotifiedUsers(notifiedUsers + USERS_PER_ITERATION);
                chatWordsRepository.save(chatWords);
                log.info("Finished reminder for word of the day");
            }
        }
    }

    private void remindForWordOfTheDay(User user) {
        if (user.getCurrentDictionary() != null) {
            Language source = user.getCurrentDictionary().getSourceLanguage();
            Language target = user.getCurrentDictionary().getTargetLanguage();
            UserWord wordOfTheDay = user.getCurrentDictionary().getRandomWord();
            if (wordOfTheDay == null) {
                log.info("Skipped for user: " + user.getName());
                return;
            }

            String translation = WordKnowledge.getEmojiByQuizCount(wordOfTheDay.getQuizCount()).getEmoji()
                    + " "
                    + wordOfTheDay.getWord().getTranslation(source)
                    + " • "
                    + wordOfTheDay.getWord().getTranslation(target)
                    + "\n\n"
                    + wordOfTheDay.getWord().getFullTranslation(source, target);

            Locale locale = new Locale(user.getLanguageCode());
            ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
            String message = String.format(messages.getString(Text.WORD_OF_THE_DAY.getValue()), translation);

            InlineKeyboardMarkup quizKeyboard = InlineKeyboardMarkup.builder()
                    .keyboard(Collections.singletonList(
                            Collections.singletonList(InlineKeyboardButton.builder()
                                    .text(messages.getString(Text.TURN_OFF_NOTIFICATIONS.getValue()))
                                    .callbackData(TurnOffWordOfTheDayNotificationsCallback.builder()
                                            .build()
                                            .code()
                                    )
                                    .build()
                            )
                    ))
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

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(user.getId())
                    .text(message)
                    .replyMarkup(quizKeyboard)
                    .disableNotification(false)
                    .parseMode(ParseMode.HTML)
                    .build();

            senderService.sendWithoutSession(sendMessage, user);
            log.info("Word of the day reminder for " + user.getName());
        }
    }
}
