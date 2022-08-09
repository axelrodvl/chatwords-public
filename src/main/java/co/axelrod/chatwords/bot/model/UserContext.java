package co.axelrod.chatwords.bot.model;

import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import co.axelrod.chatwords.storage.User;
import co.axelrod.chatwords.storage.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
public class UserContext {
    private static final String MESSAGES_BUNDLE = "messages";

    private static final ThreadLocal<Long> chatId = new ThreadLocal<>();
    private static final ThreadLocal<Integer> messageId = new ThreadLocal<>();

    private static final ThreadLocal<String> username = new ThreadLocal<>();
    private static final ThreadLocal<String> firstName = new ThreadLocal<>();
    private static final ThreadLocal<String> lastName = new ThreadLocal<>();

    private static final ThreadLocal<String> queryId = new ThreadLocal<>();
    private static final ThreadLocal<String> queryMessage = new ThreadLocal<>();

    private static final ThreadLocal<String> request = new ThreadLocal<>();
    private static final ThreadLocal<Location> location = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isCaption = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isWithPhoto = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isWithVoice = new ThreadLocal<>();

    private static final ThreadLocal<String> languageCode = new ThreadLocal<>();

    private static final ThreadLocal<User> user = new ThreadLocal<>();

    private static final ThreadLocal<String> customCallbackAlertText = new ThreadLocal<>();

    public static void close() {
        chatId.remove();
        messageId.remove();
        username.remove();
        firstName.remove();
        lastName.remove();
        queryId.remove();
        queryMessage.remove();
        request.remove();
        location.remove();
        isCaption.remove();
        isWithPhoto.remove();
        isWithVoice.remove();
        languageCode.remove();
        user.remove();
        customCallbackAlertText.remove();
    }

    public static Long getChatId() {
        return chatId.get();
    }

    public static Integer getMessageId() {
        return messageId.get();
    }

    public static String getUsername() {
        return username.get();
    }

    public static String getFirstName() {
        return firstName.get();
    }

    public static String getLastName() {
        return lastName.get();
    }

    public static String getQueryId() {
        return queryId.get();
    }

    public static String getQueryMessage() {
        return queryMessage.get();
    }

    public static String getCustomCallbackAlertText() {
        return customCallbackAlertText.get();
    }

    public static String getRequest() {
        return request.get();
    }

    public static Location getLocation() {
        return location.get();
    }

    public static Boolean getIsCaption() {
        return isCaption.get();
    }

    public static Boolean getIsWithPhoto() {
        return isWithPhoto.get();
    }

    public static Boolean getIsWithVoice() {
        return isWithVoice.get();
    }

    public static String getLanguageCode() {
        return languageCode.get();
    }

    public static User getUser() {
        return user.get();
    }

    public static void setRequest(String value) {
        request.set(value);
    }

    public static void setQueryId(String value) {
        queryId.set(value);
    }

    public static void setLocation(Location value) {
        location.set(value);
    }

    public static void setIsCaption(Boolean value) {
        isCaption.set(value);
    }

    public static void setIsWithPhoto(Boolean value) {
        isWithPhoto.set(value);
    }

    public static void setIsWithVoice(Boolean value) {
        isWithVoice.set(value);
    }

    public static void setLanguageCode(String value) {
        languageCode.set(value);
    }

    public static void setCustomCallbackAlertText(String value) {
        customCallbackAlertText.set(value);
    }

    public static void initialize(Message message, UserRepository userRepository) {
        Instant start = Instant.now();

        if (message == null || message.getChat() == null) {
            throw new ChatWordsRuntimeException("Unable to create user context");
        }

        chatId.set(message.getChat().getId());
        messageId.set(message.getMessageId());

        if (message.getText() != null && !message.getText().isBlank()) {
            request.set(message.getText());
        } else {
            request.set(message.getCaption());
        }

        location.set(message.getLocation());
        if (message.getLocation() != null) {
            request.set("");
        }

        isCaption.set(message.getCaption() != null);
        isWithPhoto.set(message.getPhoto() != null);
        isWithVoice.set(message.getVoice() != null);

        username.set(message.getFrom().getUserName());
        firstName.set(message.getFrom().getFirstName());
        lastName.set(message.getFrom().getLastName());

        user.set(userRepository.findById(chatId.get().toString()).orElse(new User(chatId.get().toString(), message.getFrom(), languageCode.get())));

        processLanguageCode(message.getFrom().getLanguageCode());

        Instant end = Instant.now();
        log.info("User context loaded in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
    }

    public static void initialize(CallbackQuery callbackQuery, UserRepository userRepository) {
        Instant start = Instant.now();

        if (callbackQuery == null || callbackQuery.getMessage().getChat() == null || callbackQuery.getData() == null) {
            throw new ChatWordsRuntimeException("Unable to create user context");
        }

        chatId.set(callbackQuery.getMessage().getChat().getId());
        messageId.set(callbackQuery.getMessage().getMessageId());
        queryId.set(callbackQuery.getId());
        queryMessage.set(callbackQuery.getMessage().getText());

        username.set(chatId.get().toString());

        request.set(callbackQuery.getData());
        isCaption.set(callbackQuery.getMessage().getCaption() != null);
        isWithPhoto.set(callbackQuery.getMessage().getPhoto() != null);
        isWithVoice.set(callbackQuery.getMessage().getVoice() != null);

        Instant start2 = Instant.now();
        user.set(userRepository.findById(chatId.get().toString()).orElseThrow());
        Instant end2 = Instant.now();
        log.info("User loaded in: " + Duration.between(start2, end2).toString()); // prints PT1M3.553S

        // There is not language in callback, so using saved or default
        if (user.get().getLanguageCode() != null) {
            languageCode.set(user.get().getLanguageCode());
        } else {
            languageCode.set(callbackQuery.getMessage().getFrom().getLanguageCode());
        }

        Instant end = Instant.now();
        log.info("User context loaded in: " + Duration.between(start, end).toString()); // prints PT1M3.553S
    }

    public static void initialize(ChatMemberUpdated chatMemberUpdated, UserRepository userRepository) {
        if (chatMemberUpdated == null || chatMemberUpdated.getChat() == null || chatMemberUpdated.getChat().getId() == null) {
            throw new ChatWordsRuntimeException("Unable to create user context");
        }

        chatId.set(chatMemberUpdated.getChat().getId());
        username.set(chatId.get().toString());
        user.set(userRepository.findById(chatId.get().toString()).orElseThrow());
    }

    private static void processLanguageCode(String value) {
        languageCode.set(value);

        // If user chosen language in settings - using it
        if (user.get().getUserSettings() != null && user.get().getUserSettings().getLanguageCode() != null) {
            languageCode.set(user.get().getUserSettings().getLanguageCode());
        }

        if (languageCode.get() == null) {
            languageCode.set(user.get().getLanguageCode());
        }
        if (languageCode.get() == null) {
            languageCode.set("en");
        }

        user.get().setLanguageCode(languageCode.get());
    }

    public static ResourceBundle getMessagesBundle() {
        if (languageCode.get().equals("ru")) {
            return ResourceBundle.getBundle(MESSAGES_BUNDLE, new Locale("ru"));
        } else if (languageCode.get().equals("uk")) {
            return ResourceBundle.getBundle(MESSAGES_BUNDLE, new Locale("uk"));
        } else {
            return ResourceBundle.getBundle(MESSAGES_BUNDLE, new Locale("en"));
        }
    }

    public static String getText(Text text) {
        return getMessagesBundle().getString(text.getValue());
    }

    public static String getText(Text text, String... values) {
        return String.format(getMessagesBundle().getString(text.getValue()), (Object[]) values);
    }
}
