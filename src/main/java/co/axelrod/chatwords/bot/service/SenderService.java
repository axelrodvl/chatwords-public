package co.axelrod.chatwords.bot.service;

import co.axelrod.chatwords.bot.command.Command;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.storage.User;
import co.axelrod.chatwords.storage.UserRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SenderService {
    private static final long DEFAULT_THROTTLING_LIMIT = 1000;

    private static final long MINUTE = 60 * 1000;
    private static final long THROTTLING_LIMIT_1_TO_10 = 1000;
    private static final long THROTTLING_LIMIT_11_TO_15 = 3000;
    private static final long THROTTLING_LIMIT_16_TO_20 = 7000;

    private final DefaultAbsSender chatWordsBot;
    private final UserRepository userRepository;
    private final RateLimiter rateLimiter;

    public SenderService(
            @Qualifier("chatWordsBot") DefaultAbsSender chatWordsBot,
            UserRepository userRepository,
            RateLimiter rateLimiter
    ) {
        this.chatWordsBot = chatWordsBot;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    @SneakyThrows
    public void completeCallback() {
        if (UserContext.getQueryId() != null) {
            if (UserContext.getCustomCallbackAlertText() == null) {
                chatWordsBot.execute(new AnswerCallbackQuery(UserContext.getQueryId()));
            } else {
                chatWordsBot.execute(
                        AnswerCallbackQuery.builder()
                                .callbackQueryId(UserContext.getQueryId())
//                                .showAlert(true)
                                .text(UserContext.getCustomCallbackAlertText())
                                .build()
                );
            }
        }
    }

    private ReplyKeyboard exitKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.EXIT))
                                .callbackData(Command.HOME.getValue())
                                .build()
                ))
                .build();
    }

    public void sendUnknownError() {
        sendMessage(Text.ERROR, exitKeyboard());
    }


    @SneakyThrows
    public void removeCommands() {
        DeleteMyCommands deleteMyCommands = DeleteMyCommands.builder()
                .scope(BotCommandScopeChat.builder().chatId(UserContext.getChatId().toString()).build())
                .build();
        chatWordsBot.execute(deleteMyCommands);
    }

    public void sendMainMenu() {
        sendCommands(Command.HOME);
    }

    @SneakyThrows
    private void sendCommands(List<Command> commands) {
        List<BotCommand> botCommands = commands.stream()
                .map(command ->
                        BotCommand.builder()
                                .command(command.getValue())
                                .description(UserContext.getText(Text.fromValue(command.getDescription())))
                                .build()
                ).collect(Collectors.toList());

        SetMyCommands setMyCommands = SetMyCommands.builder()
                .scope(BotCommandScopeChat.builder().chatId(UserContext.getChatId().toString()).build())
                .commands(botCommands)
                .build();

        chatWordsBot.execute(setMyCommands);
    }

    @SneakyThrows
    private void sendCommands(Command command) {
        sendCommands(Collections.singletonList(command));
    }

    @SneakyThrows
    public void sendTypingAction() {
        SendChatAction sendChatAction = SendChatAction.builder()
                .chatId(UserContext.getChatId().toString())
                .action("typing")
                .build();
        chatWordsBot.execute(sendChatAction);
    }

    private boolean sendInsteadOfUpdate() {
        boolean dictionaryOrMenuAfterTranslation = !UserContext.getIsCaption() &&
                (UserContext.getIsWithPhoto() || UserContext.getIsWithVoice());
        return UserContext.getQueryId() == null || dictionaryOrMenuAfterTranslation;
    }

    public void updateMessage(String message, InlineKeyboardMarkup keyboard) {
        if (sendInsteadOfUpdate()) {
            sendMessage(message, keyboard);
            return;
        }

        if (UserContext.getIsCaption()) {
            updateCaption(message, keyboard);
            return;
        }

        try {
            EditMessageText editMessageText = EditMessageText.builder()
                    .messageId(UserContext.getMessageId())
                    .chatId(UserContext.getChatId().toString())
                    .text(message)
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(keyboard)
                    .disableWebPagePreview(true)
                    .build();
            send(editMessageText);
        } catch (Exception ex) {
            sendMessage(message, keyboard);
        }
    }

    public void sendMessage(String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(UserContext.getChatId().toString())
                .text(message)
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .build();

        send(sendMessage);
    }

    public void updateMessage(String message) {
        if (sendInsteadOfUpdate()) {
            sendMessage(message);
            return;
        }

        if (UserContext.getIsCaption()) {
            updateCaption(message);
            return;
        }

        try {
            EditMessageText editMessageText = EditMessageText.builder()
                    .messageId(UserContext.getMessageId())
                    .chatId(UserContext.getChatId().toString())
                    .text(message)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .build();

            send(editMessageText);
        } catch (Exception ex) {
            sendMessage(message);
        }
    }

    public void sendMessage(String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(UserContext.getChatId().toString())
                .text(message)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .build();

        send(sendMessage);
    }

    public void sendMessage(Text text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(UserContext.getChatId().toString())
                .text(UserContext.getText(text))
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .build();

        send(sendMessage);
    }

    public void updateMessage(Text text, InlineKeyboardMarkup keyboard) {
        if (sendInsteadOfUpdate()) {
            sendMessage(text, keyboard);
            return;
        }

        if (UserContext.getIsCaption()) {
            updateCaption(UserContext.getText(text), keyboard);
            return;
        }

        try {
            EditMessageText editMessageText = EditMessageText.builder()
                    .messageId(UserContext.getMessageId())
                    .chatId(UserContext.getChatId().toString())
                    .text(UserContext.getText(text))
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(keyboard)
                    .disableWebPagePreview(true)
                    .build();

            send(editMessageText);
        } catch (Exception ex) {
            sendMessage(text, keyboard);
        }
    }

    public void sendMessage(Text text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(UserContext.getChatId().toString())
                .text(UserContext.getText(text))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true)
                .build();

        send(sendMessage);
    }

    public void updateMessage(Text text) {
        if (sendInsteadOfUpdate()) {
            sendMessage(text);
            return;
        }

        if (UserContext.getIsCaption()) {
            updateCaption(UserContext.getText(text));
            return;
        }

        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(UserContext.getChatId().toString())
                    .text(UserContext.getText(text))
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    .build();

            send(sendMessage);
        } catch (Exception ex) {
            sendMessage(text);
        }
    }

    public void sendPhoto(String message, String photoUrl, ReplyKeyboard keyboard) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(UserContext.getChatId().toString())
                .caption(message)
                .photo(new InputFile().setMedia(photoUrl))
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboard)
                .disableNotification(true)
                .build();

        send(sendPhoto);
    }

    public void updatePhoto(String message, String photoUrl, InlineKeyboardMarkup keyboard) {
        InputMediaPhoto inputMediaPhoto = InputMediaPhoto.builder()
                .caption(message)
                .media(photoUrl)
                .parseMode(ParseMode.HTML)
                .build();

        EditMessageMedia editMessageMedia = EditMessageMedia.builder()
                .messageId(UserContext.getMessageId())
                .chatId(UserContext.getChatId().toString())
                .media(inputMediaPhoto)
                .replyMarkup(keyboard)
                .build();

        send(editMessageMedia);
    }

    public void updateCaption(String message, InlineKeyboardMarkup keyboard) {
        EditMessageCaption editMessageCaption = EditMessageCaption.builder()
                .chatId(UserContext.getChatId().toString())
                .messageId(UserContext.getMessageId())
                .caption(message)
                .parseMode(ParseMode.HTML)
                .replyMarkup(keyboard)
                .build();

        send(editMessageCaption);
    }

    public void updateCaption(String message) {
        EditMessageCaption editMessageCaption = EditMessageCaption.builder()
                .chatId(UserContext.getChatId().toString())
                .messageId(UserContext.getMessageId())
                .caption(message)
                .parseMode(ParseMode.HTML)
                .build();

        send(editMessageCaption);
    }

    @SneakyThrows
    private File getFileFromResource(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }

    public void sendVoice(String message, InputFile inputFile, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVoice sendVoice = SendVoice.builder()
                .chatId(UserContext.getChatId().toString())
                .caption(message)
                .voice(inputFile)
                .parseMode(ParseMode.HTML)
                .replyMarkup(inlineKeyboardMarkup)
                .build();

        try {
            chatWordsBot.execute(sendVoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void hardThrottlingForUser() throws InterruptedException {
        // Получение текущего состояния
        Long currentTime = Instant.now().toEpochMilli();
        Long lastMessageSentAt = UserContext.getUser().getLastMessageSentTimestamp();
        Long firstMessageSentInCurrentMinute = UserContext.getUser().getFirstMessageSentInCurrentMinute();
        Integer messagesSentInLastMinute = UserContext.getUser().getMessageSentInLastMinute();

        // Выставление состояние впервые
        if (lastMessageSentAt == null || firstMessageSentInCurrentMinute == null) {
            UserContext.getUser().setMessageSentInLastMinute(0);
            UserContext.getUser().setFirstMessageSentInCurrentMinute(currentTime);
            UserContext.getUser().setLastMessageSentTimestamp(currentTime);
            messagesSentInLastMinute = 0;
            firstMessageSentInCurrentMinute = currentTime;
            lastMessageSentAt = currentTime;
        }

        // Обнуление состояния, если минута прошла
        if (currentTime - firstMessageSentInCurrentMinute > MINUTE) {
            UserContext.getUser().setMessageSentInLastMinute(0);
            UserContext.getUser().setFirstMessageSentInCurrentMinute(currentTime);
            UserContext.getUser().setLastMessageSentTimestamp(currentTime);
            messagesSentInLastMinute = 0;
            firstMessageSentInCurrentMinute = currentTime;
            lastMessageSentAt = currentTime;
        }

        // Троттлинг
        long diff = currentTime - lastMessageSentAt;
        long throttling = 0;
        if (messagesSentInLastMinute > 0 && messagesSentInLastMinute < 10) {
            throttling = diff < THROTTLING_LIMIT_1_TO_10 ? THROTTLING_LIMIT_1_TO_10 - diff : THROTTLING_LIMIT_1_TO_10;
        } else if (messagesSentInLastMinute > 10 && messagesSentInLastMinute < 15) {
            throttling = diff < THROTTLING_LIMIT_11_TO_15 ? THROTTLING_LIMIT_11_TO_15 - diff : THROTTLING_LIMIT_11_TO_15;
        } else if (messagesSentInLastMinute > 15) {
            throttling = diff < THROTTLING_LIMIT_16_TO_20 ? THROTTLING_LIMIT_16_TO_20 - diff : THROTTLING_LIMIT_16_TO_20;
        }
        if (throttling > 0) {
            log.warn("Throttling for " + throttling + " ms (" + messagesSentInLastMinute + ")");
            Thread.sleep(throttling);
        }

        // Сохранение состояния
        UserContext.getUser().setMessageSentInLastMinute(messagesSentInLastMinute + 1);
        UserContext.getUser().setFirstMessageSentInCurrentMinute(firstMessageSentInCurrentMinute);
        UserContext.getUser().setLastMessageSentTimestamp(currentTime);
    }

    private void softThrottlingForUser() throws InterruptedException {
        if (UserContext.getUser().getLastMessageSentTimestamp() != null) {
            Long lastMessageSentAt = UserContext.getUser().getLastMessageSentTimestamp();
            Long currentTime = Instant.now().toEpochMilli();
            long diff = currentTime - lastMessageSentAt;
            if (diff < DEFAULT_THROTTLING_LIMIT) {
                log.warn("Throttling for " + diff + " ms");
                Thread.sleep(diff);
            }
            UserContext.getUser().setLastMessageSentTimestamp(Instant.now().toEpochMilli());
        }
    }

    @SneakyThrows
    public void sendWithoutSession(SendMessage sendMessage, User user) {
        String text = sendMessage.getText();
        sendMessage.setText(fixWidth(sendMessage.getText()));
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(sendMessage)
            );
            Try.run(restrictedCall).onFailure(failure -> sendFailureHandler(failure, user));
            completeCallback();

            log.info("To " + sendMessage.getChatId() + ": " + text);
        }
    }

    private void sendFailureHandler(Throwable throwable, User user) {
        log.warn("Avoiding 429 error from Telegram API", throwable);
        if (throwable instanceof TelegramApiRequestException) {
            TelegramApiRequestException telegramApiRequestException = (TelegramApiRequestException) throwable;
            if (telegramApiRequestException.getErrorCode().equals(403) && telegramApiRequestException.getApiResponse().equals("Forbidden: bot was blocked by the user")) {
                user.setBotDeleted(true);
                userRepository.save(user);
            }
        }
    }

    private void sendFailureHandler(Throwable throwable) {
        log.warn("Avoiding 429 error from Telegram API", throwable);
        if (throwable instanceof TelegramApiRequestException) {
            TelegramApiRequestException telegramApiRequestException = (TelegramApiRequestException) throwable;
            if (telegramApiRequestException.getErrorCode().equals(403) && telegramApiRequestException.getApiResponse().equals("Forbidden: bot was blocked by the user")) {
                UserContext.getUser().setBotDeleted(true);
                userRepository.save(UserContext.getUser());
            }
        }
    }

    @SneakyThrows
    public void send(SendMessage sendMessage) {
        if (UserContext.getUser().getHasMainMenu() != null) {
            UserContext.getUser().setHasMainMenu(null);
            sendMainMenu();
        }

//        softThrottlingForUser();
        userRepository.save(UserContext.getUser());

        //        softThrottlingForUser();

        Instant start = Instant.now();
        String text = sendMessage.getText();
        sendMessage.setText(fixWidth(sendMessage.getText()));
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(sendMessage)
            );
            Try.run(restrictedCall).onFailure(this::sendFailureHandler);
            completeCallback();

            log.info("To " + UserContext.getUsername() + ": " + text);

            Instant end = Instant.now();
            log.info("Message sent in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
        }
    }

    private String fixWidth(String text) {
        return text;
//        String[] strings = text.split("\n");
//        String firstString = strings[0];
//        if (firstString.length() < 35) {
//            int symbolsToAddAtTheEnd = 35 - firstString.length();
//
//            StringBuilder spaces = new StringBuilder();
//            for(int i = 0; i < symbolsToAddAtTheEnd; i++) {
//                spaces.append("&#8200;");
//            }
//
////            String trailing = "<a href=\"\">" + spaces + "</a>";
//            String trailing = " <pre>" + spaces + "</pre>" + "<a href=\"\">" + spaces + "</a>";
//
//            strings[0] = strings[0] + trailing;
//        }
//        String output = String.join("\n", strings);
//        return output;
    }

    @SneakyThrows
    public void send(EditMessageText editMessageText) {
        if (UserContext.getUser().getHasMainMenu() != null) {
            UserContext.getUser().setHasMainMenu(null);
            sendMainMenu();
        }

//        softThrottlingForUser();
        Instant start;
        Instant end;

        start = Instant.now();
        userRepository.save(UserContext.getUser());
        end = Instant.now();
        log.info("User saved in: " + Duration.between(start, end).toString()); // prints PT1M3.553S

        start = Instant.now();
        String text = editMessageText.getText();
        editMessageText.setText(fixWidth(editMessageText.getText()));
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(editMessageText)
            );
            Try.run(restrictedCall).onFailure(this::sendFailureHandler);
            completeCallback();

            log.info("To " + UserContext.getUsername() + ": " + text);

            end = Instant.now();
            log.info("Message sent in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
        }
    }

    @SneakyThrows
    public void send(SendPhoto sendPhoto) {
        if (UserContext.getUser().getHasMainMenu() != null) {
            UserContext.getUser().setHasMainMenu(null);
            sendMainMenu();
        }

//        softThrottlingForUser();
        Instant start;
        Instant end;

        start = Instant.now();
        userRepository.save(UserContext.getUser());
        end = Instant.now();
        log.info("User saved in: " + Duration.between(start, end).toString()); // prints PT1M3.553S

        start = Instant.now();
        String text = sendPhoto.getCaption();
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(sendPhoto)
            );
            Try.run(restrictedCall).onFailure(this::sendFailureHandler);
            completeCallback();

            log.info("To " + UserContext.getUsername() + ": " + text);

            end = Instant.now();
            log.info("Message sent in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
        }
    }

    @SneakyThrows
    public void send(EditMessageCaption editMessageCaption) {
        if (UserContext.getUser().getHasMainMenu() != null) {
            UserContext.getUser().setHasMainMenu(null);
            sendMainMenu();
        }

//        softThrottlingForUser();
        Instant start;
        Instant end;

        start = Instant.now();
        userRepository.save(UserContext.getUser());
        end = Instant.now();
        log.info("User saved in: " + Duration.between(start, end).toString()); // prints PT1M3.553S

        start = Instant.now();
        String text = editMessageCaption.getCaption();
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(editMessageCaption)
            );
            Try.run(restrictedCall).onFailure(this::sendFailureHandler);
            completeCallback();

            log.info("To " + UserContext.getUsername() + ": " + text);

            end = Instant.now();
            log.info("Message sent in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
        }
    }

    @SneakyThrows
    public void send(EditMessageMedia editMessageMedia) {
        if (UserContext.getUser().getHasMainMenu() != null) {
            UserContext.getUser().setHasMainMenu(null);
            sendMainMenu();
        }

//        softThrottlingForUser();
        Instant start;
        Instant end;

        start = Instant.now();
        userRepository.save(UserContext.getUser());
        end = Instant.now();
        log.info("User saved in: " + Duration.between(start, end).toString()); // prints PT1M3.553S

        start = Instant.now();
        String text = editMessageMedia.getMedia().getCaption();
        if (text.length() > 4096) {
            log.warn("Too long message");
        } else {
            CheckedRunnable restrictedCall = RateLimiter.decorateCheckedRunnable(
                    rateLimiter,
                    () -> chatWordsBot.execute(editMessageMedia)
            );
            Try.run(restrictedCall).onFailure(this::sendFailureHandler);
            completeCallback();

            log.info("To " + UserContext.getUsername() + ": " + text);

            end = Instant.now();
            log.info("Message sent in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
        }
    }
}
