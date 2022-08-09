package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.CancelCallback;
import co.axelrod.chatwords.bot.model.callback.Oxford3000LevelCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.dictionary.oxford.Level;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.Oxford3000DictionaryStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
public class Oxford3000CommandProcessor extends CommandProcessor<Oxford3000DictionaryStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public Oxford3000CommandProcessor(
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
        if (UserContext.getUser().getOxford3000Added() != null && UserContext.getUser().getOxford3000Added()) {
            analytics.log(UserEvent.SETTING_OXFORD_3000_DICTIONARY);

            UserContext.getUser().setCurrentDictionary(UserContext.getUser().getDictionaries().get(
                    Level.getDictionaryName(UserContext.getUser().getLevel())
            ));
            showDictionaryCommandProcessor.process();
            return;
        }

        Oxford3000DictionaryStory story = getStory();

        if (story == null) {
            UserContext.getUser().setOxford3000DictionaryStory(new Oxford3000DictionaryStory());

            analytics.log(UserEvent.CREATING_OXFORD_3000_DICTIONARY);
            senderService.updateMessage(Text.LEARN_OXFORD_3000_WORDS, chooseYourLevelKeyboard());
        } else {
            Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
            if (callback != null && callback.getId().equals(Callback.CANCEL.getId())) {
                UserContext.getUser().clearStories();
                mainMenuCommandProcessor.processRequest();
            } else {
                Level level = null;
                callback = CallbackUtil.getCallback(UserContext.getRequest());
                if (callback != null && callback.getId().equals(Callback.OXFORD_3000_LEVEL.getId())) {
                    Oxford3000LevelCallback oxford3000LevelCallback = new Oxford3000LevelCallback(UserContext.getRequest());
                    level = Level.valueOf(oxford3000LevelCallback.getLevel());
                }

                if (level == null) {
                    senderService.updateMessage(Text.INVALID_LEVEL, chooseYourLevelKeyboard());
                    return;
                }

                userService.createOxford3000Dictionary(level);

                UserContext.getUser().clearStories();

                UserContext.getUser().setOxford3000Added(true);
                UserContext.getUser().setLevel(level);
                analytics.log(UserEvent.getNewOxford3000LevelDictionaryCreated(level));
                showDictionaryCommandProcessor.process();
            }
        }
    }

    private InlineKeyboardMarkup chooseYourLevelKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.LEVEL_A1))
                                .callbackData(Oxford3000LevelCallback.builder()
                                        .level(Level.A1.name())
                                        .dontKnow(false)
                                        .build().code())
                                .build()
                )
        );

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.LEVEL_A2))
                                .callbackData(Oxford3000LevelCallback.builder()
                                        .level(Level.A2.name())
                                        .dontKnow(false)
                                        .build().code())
                                .build()
                )
        );

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.LEVEL_B1))
                                .callbackData(Oxford3000LevelCallback.builder()
                                        .level(Level.B1.name())
                                        .dontKnow(false)
                                        .build().code())
                                .build()
                )
        );

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.LEVEL_B2))
                                .callbackData(Oxford3000LevelCallback.builder()
                                        .level(Level.B2.name())
                                        .dontKnow(false)
                                        .build().code())
                                .build()
                )
        );

//        builder.keyboardRow(Collections.singletonList(
//                        InlineKeyboardButton.builder()
//                                .text(UserContext.getText(Text.LEVEL_DONT_KNOW))
//                                .callbackData(Oxford3000LevelCallback.builder()
//                                        .level("Don't know")
//                                        .dontKnow(false)
//                                        .build().code())
//                                .build()
//                )
//        );

        builder.keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_BUTTON))
                                .callbackData(CancelCallback.builder().build().code())
                                .build()
                )
        );

        return builder.build();
    }

    @Override
    protected Oxford3000DictionaryStory getStory() {
        return UserContext.getUser().getOxford3000DictionaryStory();
    }
}
