package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.DeleteWordCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserDictionary;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.story.NoStory;
import co.axelrod.chatwords.util.HashUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RemoveWordCallbackCommandProcessor extends CommandProcessor<NoStory> {
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    public RemoveWordCallbackCommandProcessor(
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
        DeleteWordCallback deleteWordCallback = new DeleteWordCallback(UserContext.getRequest());

        if (deleteWordCallback.getWordId() != null && deleteWordCallback.getDictionaryIdHash() != null) {
            Optional<UserDictionary> targetUserDictionary = UserContext.getUser().getDictionaries().entrySet().stream()
                    .filter(entry -> HashUtil.getHash(entry.getKey()).equals(deleteWordCallback.getDictionaryIdHash()))
                    .map(Map.Entry::getValue)
                    .findFirst();

            if (targetUserDictionary.isEmpty()) {
                return;
            }
            UserDictionary userDictionary = targetUserDictionary.get();

            // Костыль - общий словарь
            Optional<UserWord> userWord = userDictionary.getWords()
                    .stream()
                    .filter(uw -> uw.getWord().getId().equals(deleteWordCallback.getWordId()))
                    .findFirst();

            userWord.ifPresent(word -> userDictionary
                    .getWords()
                    .remove(word)
            );

            // Костыль - текущий словарь
            userWord = UserContext.getUser().getCurrentDictionary()
                    .getWords()
                    .stream()
                    .filter(uw -> uw.getWord().getId().equals(deleteWordCallback.getWordId()))
                    .findFirst();

            userWord.ifPresent(word -> UserContext.getUser().getCurrentDictionary()
                    .getWords()
                    .remove(word)
            );
        }

        UserContext.setCustomCallbackAlertText(
                String.format(
                        UserContext.getText(Text.WORD_HAS_BEEN_DELETED_FROM_DICTIONARY),
                        UserContext.getUser().getCurrentDictionary().getName(),
                        UserContext.getUser().getCurrentDictionary().getSourceLanguage().getEmoji(),
                        UserContext.getUser().getCurrentDictionary().getTargetLanguage().getEmoji()
                )
        );

        analytics.log(UserEvent.REMOVED_WORD);
        showDictionaryCommandProcessor.process();
    }


    @Override
    protected NoStory getStory() {
        return null;
    }
}
