package co.axelrod.chatwords.bot.command.message.dictionary;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.formatter.DictionaryFormatter;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.paging.Paging;
import co.axelrod.chatwords.storage.paging.PagingScreen;
import co.axelrod.chatwords.storage.story.NoStory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ShowDetailedDictionaryCommandProcessor extends ShowDictionaryAbstractCommandProcessor {
    private static final int ELEMENTS_ON_SCREEN = 1;

    public ShowDetailedDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
    }

    @Override
    protected void processRequest() {
        processShowHideLearned();

        Paging paging = UserContext.getUser().getPaging();

        if (paging == null || !paging.getPagingScreen().equals(PagingScreen.DETAILED_DICTIONARY)) {
            paging = new Paging(PagingScreen.DETAILED_DICTIONARY, ELEMENTS_ON_SCREEN);
            UserContext.getUser().setPaging(paging);
        }

        if (UserContext.getRequest().equals(Callback.CHANGE_DIRECTION_DETAILED.getValue())) {
            analytics.log(UserEvent.CHANGED_DICTIONARY_DIRECTION);
            Language oldSource = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
            UserContext.getUser().getCurrentDictionary().setSourceLanguage(UserContext.getUser().getCurrentDictionary().getTargetLanguage());
            UserContext.getUser().getCurrentDictionary().setTargetLanguage(oldSource);
        }

        StringBuilder response = new StringBuilder();
        response.append(DictionaryFormatter.getDictionary(UserContext.getUser().getCurrentDictionary()));
        response.append("\n");

        List<String> values = paging.getPage(UserContext.getUser().getCurrentDictionary().getFilteredWords());

        if (UserContext.getRequest().equals(Callback.NEXT_PAGE.getValue())) {
            analytics.log(UserEvent.DETAILED_DICTIONARY_NEXT_PAGE);
            values = paging.nextPage(UserContext.getUser().getCurrentDictionary().getFilteredWords());
        }
        if (UserContext.getRequest().equals(Callback.PREVIOUS_PAGE.getValue())) {
            analytics.log(UserEvent.DETAILED_DICTIONARY_PREVIOUS_PAGE);
            values = paging.previousPage(UserContext.getUser().getCurrentDictionary().getFilteredWords());
        }

        if (UserContext.getUser().getCurrentDictionary().getWords().isEmpty()) {
            response.append(UserContext.getText(Text.DICTIONARY_IS_EMPTY));
            analytics.log(UserEvent.SHOWING_EMPTY_DICTIONARY);
            senderService.updateMessage(response.toString());
        } else {
            response.append(String.join("", values));
            analytics.log(UserEvent.SHOWING_DETAILED_DICTIONARY);
            senderService.updateMessage(response.toString(), getDictionaryKeyboard());
        }
    }

    @Override
    protected NoStory getStory() {
        return null;
    }

    private InlineKeyboardMarkup getDictionaryKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.WITHOUT_DETAILS))
                                .callbackData(Callback.SHOW_DICTIONARY.getValue())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.ALTER_DICTIONARY))
                                .callbackData(Callback.ALTER_DICTIONARY.getValue())
                                .build()
                ))
                .keyboardRow(Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(
                                        UserContext.getUser().getCurrentDictionary().getSourceLanguage().getEmoji()
                                                + " " + UserContext.getText(Text.CHANGE_DIRECTION)
                                )
                                .callbackData(Callback.CHANGE_DIRECTION_DETAILED.getValue())
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(UserContext.getUser().getCurrentDictionary().getSort().getEmoji() + " " + UserContext.getText(Text.SORTING))
                                .callbackData(Callback.CHANGE_SORTING.getValue())
                                .build()
                ))
                .keyboardRow(getShowHideLearnedKeyboardRow())
                .keyboardRow(getPagingAndQuizKeyboardRow())
                .keyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.MENU_BUTTON))
                                        .callbackData(UserContext.getText(Text.EXIT))
                                        .build()
                        )
                )
                .build();
    }
}
