package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.ChangeDictionaryStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ChangeDictionaryCommandProcessor extends CommandProcessor<ChangeDictionaryStory> {
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public ChangeDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            ShowDictionaryCommandProcessor showDictionaryCommandProcessor) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.showDictionaryCommandProcessor = showDictionaryCommandProcessor;
    }

    @Override
    protected void processRequest() {
        ChangeDictionaryStory story = getStory();

        if (story == null) {
            UserContext.getUser().setChangeDictionaryStory(new ChangeDictionaryStory());
            analytics.log(UserEvent.CHANGING_DICTIONARY);
            senderService.updateMessage(Text.CHOOSE_DICTIONARY, dictionariesKeyboard());
        } else {
            String dictionaryName = UserContext.getRequest();

            boolean chosen = UserContext.getUser().getDictionaries().containsKey(dictionaryName);
            if (!chosen && UserContext.getRequest().length() > 9) {
                dictionaryName = dictionaryName.substring(9);
                chosen = UserContext.getUser().getDictionaries().containsKey(dictionaryName);
            }
            if (chosen) {
                String currentDictionaryName = UserContext.getUser().getCurrentDictionary().getName();
                UserContext.getUser().getDictionaries().put(currentDictionaryName, UserContext.getUser().getCurrentDictionary());
                UserContext.getUser().setCurrentDictionary(UserContext.getUser().getDictionaries().get(dictionaryName));
                UserContext.getUser().clearStories();
                analytics.log(UserEvent.CHANGED_DICTIONARY);
                showDictionaryCommandProcessor.processRequest();
            } else {
                analytics.log(UserEvent.CHOSEN_INVALID_DICTIONARY);
                senderService.updateMessage(Text.CHOOSE_EXISTING_DICTIONARY, dictionariesKeyboard());
            }
        }
    }

    @Override
    protected ChangeDictionaryStory getStory() {
        return UserContext.getUser().getChangeDictionaryStory();
    }

    private InlineKeyboardMarkup dictionariesKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(
                        UserContext.getUser().getDictionaries().entrySet().stream()
                                .map(entry -> InlineKeyboardButton.builder()
                                        .text(entry.getValue().getSourceLanguage().getEmoji() +
                                                entry.getValue().getTargetLanguage().getEmoji() + " " +
                                                entry.getKey())
                                        .callbackData(entry.getKey())
                                        .build())
                                .map(Collections::singletonList)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
