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
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.ThousandWordsDictionaryStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ThousandWordsCommandProcessor extends CommandProcessor<ThousandWordsDictionaryStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public ThousandWordsCommandProcessor(
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
        if (UserContext.getUser().getThousandWordsAdded() != null && UserContext.getUser().getThousandWordsAdded()) {
            analytics.log(UserEvent.SETTING_1000_WORDS_DICTIONARY);

            UserContext.getUser().setCurrentDictionary(userService.get1000WordsDictionary());
            showDictionaryCommandProcessor.process();
            return;
        }

        ThousandWordsDictionaryStory story = getStory();

        if (story == null) {
            UserContext.getUser().setThousandWordsDictionaryStory(new ThousandWordsDictionaryStory());

            List<String> keyboardButtons = Arrays.stream(Language.values())
                    .filter(language -> !language.getFullName().equals(UserContext.getUser().getDefaultFromLanguage().getFullName()))
                    .map(language -> language.getEmoji() + " " + UserContext.getMessagesBundle().getString(language.getFullName()))
                    .collect(Collectors.toList());
            keyboardButtons.add(UserContext.getText(Text.CANCEL));

            analytics.log(UserEvent.CREATING_1000_WORDS_DICTIONARY);
            senderService.updateMessage(Text.LEARN_1000_WORDS, languagesKeyboard());
        } else {
            Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
            if (callback != null && callback.getId().equals(Callback.CANCEL.getId())) {
                UserContext.getUser().clearStories();
                mainMenuCommandProcessor.processRequest();
            } else {
                Language targetLanguage = null;
                callback = CallbackUtil.getCallback(UserContext.getRequest());
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

                userService.create1000WordsDictionary(targetLanguage);

                UserContext.getUser().clearStories();

                UserContext.getUser().setThousandWordsAdded(true);
                analytics.log(UserEvent.NEW_1000_WORDS_DICTIONARY_CREATED);
                showDictionaryCommandProcessor.process();
            }
        }
    }

    private InlineKeyboardMarkup languagesKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        Arrays.stream(Language.values())
                .filter(language -> !language.equals(UserContext.getUser().getDefaultFromLanguage()))
                .filter(language -> {
                    if (UserContext.getUser().getDefaultFromLanguage().equals(Language.UKRAINIAN)) {
//                        return language.isUkrainianSupport();
                        return language.equals(Language.ENGLISH);
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

    private InlineKeyboardMarkup startQuizKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.START_QUIZ))
                        .callbackData(Callback.QUIZ.getValue())
                        .build()))
                .build();
    }

    @Override
    protected ThousandWordsDictionaryStory getStory() {
        return UserContext.getUser().getThousandWordsDictionaryStory();
    }
}
