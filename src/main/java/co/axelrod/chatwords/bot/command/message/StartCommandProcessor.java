package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.LanguageCodeCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.StartStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

@Component
public class StartCommandProcessor extends CommandProcessor<StartStory> {
    public static final String FIRST_WORD = "Привет";

    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public StartCommandProcessor(
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
    protected void processRequest() {
        boolean userInitialized = !UserContext.getUser().getDictionaries().isEmpty() && UserContext.getUser().getCurrentDictionary() != null;
        if (userInitialized && UserContext.getUser().getStartStory() == null) {
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.process();
            return;
        }

        StartStory story = getStory();

        if (story == null) {
            analytics.log(UserEvent.START);

            senderService.removeCommands();

            // Saving Deep Link
            if (UserContext.getRequest().contains("/start") && UserContext.getRequest().split(" ").length > 1) {
                String deepLinkInBase64 = UserContext.getRequest().split(" ")[1];
                String decodedBase64 = new String(Base64.getDecoder().decode(deepLinkInBase64));
                UserContext.getUser().setDeepLink(decodedBase64);
                analytics.logCampaign(decodedBase64);
            }

            UserContext.getUser().setStartStory(new StartStory());

            // Saving default user language based on language code
            Language defaultFromLanguage = Language.RUSSIAN;
            if (Language.getLanguageById(UserContext.getLanguageCode()).equals(Language.UKRAINIAN)) {
                defaultFromLanguage = Language.UKRAINIAN;
            }
            UserContext.getUser().setDefaultFromLanguage(defaultFromLanguage);

            senderService.sendMessage(Text.CHOOSE_LEARNING_LANGUAGE, toLanguagesKeyboard(UserContext.getUser().getDefaultFromLanguage()));
            return;
        }

        if (story.getDefaultToLanguage() == null) {
            Language language = null;

            LanguageCodeCallback languageCodeCallback = new LanguageCodeCallback(UserContext.getRequest());
            if (languageCodeCallback.getLanguageCode() != null) {
                language = Language.getLanguageById(languageCodeCallback.getLanguageCode());
            }

            if (language == null) {
                senderService.updateMessage(Text.CHOOSE_LEARNING_LANGUAGE, toLanguagesKeyboard(UserContext.getUser().getDefaultFromLanguage()));
                return;
            }

            analytics.log(UserEvent.getTargetLanguage(language));
            UserContext.getUser().getStartStory().setDefaultToLanguage(language);
            UserContext.getUser().setDefaultToLanguage(UserContext.getUser().getStartStory().getDefaultToLanguage());

            String dictionaryName = UserContext.getText(Text.fromValue(language.getFullName()));
            userService.setToLanguage(language);
            userService.createDictionary(
                    dictionaryName,
                    UserContext.getUser().getDefaultFromLanguage(),
                    UserContext.getUser().getDefaultToLanguage());
        }

        UserContext.getUser().clearStories();
        analytics.log(UserEvent.PASSED_INTRODUCTION);
        senderService.sendMainMenu();
        senderService.updateMessage(Text.START_TUTORIAL, firstWordKeyboard());
    }

    private InlineKeyboardMarkup toLanguagesKeyboard(Language from) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        if (from.equals(Language.UKRAINIAN)) {
            Arrays.stream(Language.values())
                    .filter(language -> !language.getFullName().equals(UserContext.getUser().getDefaultFromLanguage().getFullName()))
                    .filter(Language::isUkrainianSupport)
                    .forEach(language -> builder.keyboardRow(Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                                    .callbackData(LanguageCodeCallback.builder()
                                            .languageCode(language.getValue())
                                            .build()
                                            .code())
                                    .build()
                    )));
        } else {
            Arrays.stream(Language.values())
                    .filter(language -> !language.getFullName().equals(UserContext.getUser().getDefaultFromLanguage().getFullName()))
                    .forEach(language -> builder.keyboardRow(Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                                    .callbackData(LanguageCodeCallback.builder()
                                            .languageCode(language.getValue())
                                            .build()
                                            .code())
                                    .build()
                    )));
        }

        return builder.build();
    }

    @Override
    protected StartStory getStory() {
        return UserContext.getUser().getStartStory();
    }

    private InlineKeyboardMarkup firstWordKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(FIRST_WORD)
                                .callbackData(FIRST_WORD)
                                .build()
                ))
                .build();
    }
}
