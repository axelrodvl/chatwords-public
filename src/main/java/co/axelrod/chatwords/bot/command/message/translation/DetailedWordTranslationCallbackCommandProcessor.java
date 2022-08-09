package co.axelrod.chatwords.bot.command.message.translation;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.DetailedWordTranslationCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.*;
import co.axelrod.chatwords.storage.story.NoStory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DetailedWordTranslationCallbackCommandProcessor extends CommandProcessor<NoStory> {
    private final WordRepository wordRepository;

    public DetailedWordTranslationCallbackCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            WordRepository wordRepository
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.wordRepository = wordRepository;
    }

    @Override
    protected void processRequest() {
        DetailedWordTranslationCallback detailedWordTranslationCallback = new DetailedWordTranslationCallback(UserContext.getRequest());

        Language source = Language.getLanguageById(detailedWordTranslationCallback.getLanguageFrom());
        Language target = Language.getLanguageById(detailedWordTranslationCallback.getLanguageTo());

        Word word = getWord(detailedWordTranslationCallback);
        if (word == null) {
            log.error("Word for detailed translation not found");
            return;
        }

        boolean wordFromCurrentDictionary = wordExistsInCurrentDictionary(word);

        analytics.log(UserEvent.DETAILED_WORD_TRANSLATION);
        senderService.updateMessage(
                word.getDetailedFormattedTranslation(source, target),
                TranslateCommandProcessor.translatedWordKeyboard(word,
                        !wordFromCurrentDictionary,
                        false,
                        source,
                        target)
        );
    }

    private boolean wordExistsInCurrentDictionary(Word word) {
        return UserContext.getUser().getCurrentDictionary().getWords().stream()
                .anyMatch(userWord -> userWord.getWord().getId().equals(word.getId()));
    }

    private Word getWord(DetailedWordTranslationCallback callback) {
        // Поиск в текущем словаре
        Word wordInCurrentDictionary = getWordById(UserContext.getUser().getCurrentDictionary(), callback.getWordId());
        if (wordInCurrentDictionary != null) {
            return wordInCurrentDictionary;
        }

        // Слово отсутствует в текущем словаре, поиск слова по общему словарю
        return wordRepository.findById(callback.getWordId()).orElse(null);
    }

    private Word getWordById(UserDictionary userDictionary, String wordId) {
        return userDictionary.getWords().stream()
                .map(UserWord::getWord)
                .filter(word -> wordId.equals(word.getId()))
                .findFirst().orElse(null);
    }

    @Override
    protected NoStory getStory() {
        return null;
    }
}
