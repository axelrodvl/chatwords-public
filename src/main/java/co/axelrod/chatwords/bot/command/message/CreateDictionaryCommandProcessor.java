package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.model.callback.LanguageCodeCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.model.ServiceResponse;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.CreateDictionaryStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;

@Component
public class CreateDictionaryCommandProcessor extends CommandProcessor<CreateDictionaryStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public CreateDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            MainMenuCommandProcessor mainMenuCommandProcessor,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.mainMenuCommandProcessor = mainMenuCommandProcessor;
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
    }

    @Override
    protected void processRequest() {
        CreateDictionaryStory story = getStory();

        if (story == null) {
            UserContext.getUser().setCreateDictionaryStory(new CreateDictionaryStory());
            UserContext.getUser().getCreateDictionaryStory().setEnterLanguage(true);
            analytics.log(UserEvent.CREATING_DICTIONARY);
            senderService.updateMessage(Text.CHOOSE_TARGET_LANGUAGE, languagesKeyboard());
            return;
        }

        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.getId().equals(Callback.CANCEL.getId())) {
            UserContext.getUser().clearStories();
            mainMenuCommandProcessor.process();
            return;
        }

        // Создаем словарь (ввод направления)
        if (Boolean.TRUE.equals(story.getEnterLanguage())) {
            Language targetLanguage = null;
            if (callback != null && callback.getId().equals(Callback.LANGUAGE_CODE.getId())) {
                LanguageCodeCallback languageCodeCallback = new LanguageCodeCallback(UserContext.getRequest());
                if (languageCodeCallback.getLanguageCode() != null) {
                    targetLanguage = Language.getLanguageById(languageCodeCallback.getLanguageCode());
                }
            }

            if (targetLanguage == null) {
                analytics.log(UserEvent.ENTERED_DICTIONARY_TARGET_LANGUAGE);
                senderService.updateMessage(Text.INVALID_DICTIONARY_TARGET_LANGUAGE, languagesKeyboard());
                return;
            }

            UserContext.getUser().getCreateDictionaryStory().setTargetLanguage(targetLanguage);
            UserContext.getUser().getCreateDictionaryStory().setEnterLanguage(false);
            UserContext.getUser().getCreateDictionaryStory().setEnterName(true);

            analytics.log(UserEvent.ENTERED_DICTIONARY_TARGET_LANGUAGE);
            senderService.updateMessage(Text.SET_DICTIONARY_NAME_OR_CHOOSE_SUGGESTED, defaultNameKeyboard(targetLanguage));
            return;
        }

        // Создаем словарь (ввод имени)
        if (Boolean.TRUE.equals(story.getEnterName())) {
            ServiceResponse serviceResponse = userService.createDictionary(UserContext.getRequest(), UserContext.getUser().getDefaultFromLanguage(), story.getTargetLanguage());
            if (!serviceResponse.isSuccess()) {
                analytics.log(serviceResponse.getUserEvent());
                senderService.updateMessage(serviceResponse.getText());
                return;
            }

            UserContext.getUser().clearStories();
            analytics.log(UserEvent.NEW_DICTIONARY_CREATED);
            showDictionaryCommandProcessor.processRequest();
        }
    }

    @Override
    protected CreateDictionaryStory getStory() {
        return UserContext.getUser().getCreateDictionaryStory();
    }

    private InlineKeyboardMarkup languagesKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        Arrays.stream(Language.values())
                .filter(language -> !language.equals(UserContext.getUser().getDefaultFromLanguage()))
                .filter(language -> {
                    if (UserContext.getUser().getDefaultFromLanguage().equals(Language.UKRAINIAN)) {
                        return language.isUkrainianSupport();
                    } else {
                        return true;
                    }
                })
                .forEach(language -> builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                                .callbackData(LanguageCodeCallback.builder()
                                        .languageCode(language.getValue())
                                        .build()
                                        .code())
                                .build()
                )));

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_BUTTON))
                                .callbackData(CancelCallback.builder().build().code())
                                .build()
                )
        );

        return builder.build();
    }

    private InlineKeyboardMarkup defaultNameKeyboard(Language targetLanguage) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.fromValue(targetLanguage.getFullName())))
                                .callbackData(UserContext.getText(Text.fromValue(targetLanguage.getFullName())))
                                .build())
                )
                .build();
    }
}