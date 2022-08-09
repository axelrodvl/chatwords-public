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
import co.axelrod.chatwords.storage.sorting.Sort;
import co.axelrod.chatwords.storage.story.NoStory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ShowDictionaryCommandProcessor extends ShowDictionaryAbstractCommandProcessor {
    private static final int ELEMENTS_ON_SCREEN = 10;

    public ShowDictionaryCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
    }

    @Override
    public void processRequest() {
        UserContext.setIsCaption(false);
        processShowHideLearned();

        Paging paging = UserContext.getUser().getPaging();
        if (paging == null || !paging.getPagingScreen().equals(PagingScreen.DICTIONARY)) {
            paging = new Paging(PagingScreen.DICTIONARY, ELEMENTS_ON_SCREEN);
            UserContext.getUser().setPaging(paging);
        }

        if (UserContext.getRequest().equals(Callback.CHANGE_DIRECTION.getValue())) {
            analytics.log(UserEvent.CHANGED_DICTIONARY_DIRECTION);
            Language oldSource = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
            UserContext.getUser().getCurrentDictionary().setSourceLanguage(UserContext.getUser().getCurrentDictionary().getTargetLanguage());
            UserContext.getUser().getCurrentDictionary().setTargetLanguage(oldSource);
        }

        if (UserContext.getRequest().equals(Callback.CHANGE_SORTING.getValue())) {
            analytics.log(UserEvent.CHANGED_DICTIONARY_SORTING);
            if (UserContext.getUser().getCurrentDictionary().getSort().equals(Sort.OLD_TO_NEW)) {
                UserContext.setCustomCallbackAlertText(UserContext.getText(Text.ORDER_NEW_TO_OLD));
            } else {
                UserContext.setCustomCallbackAlertText(UserContext.getText(Text.ORDER_OLD_TO_NEW));
            }
            UserContext.getUser().getCurrentDictionary().reverseSort();
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

//            // Showing ads
//            response.append("\n\n");
//            response.append(UserContext.getText(Text.HINT_2));
            senderService.updateMessage(response.toString(), emptyDictionaryKeyboard());
        } else {
            response.append(String.join("\n", values));

//            // Showing ads
//            if (!values.isEmpty()) {
//                response.append("\n\n");
//            }
//            response.append(UserContext.getText(Text.HINT_2));

            analytics.log(UserEvent.SHOWING_DICTIONARY);
//            UserContext.setCustomCallbackAlertText(String.format(UserContext.getText(Text.SHOW_DICTIONARY_ALERT), UserContext.getUser().getCurrentDictionary().getName()));
            senderService.updateMessage(response.toString(), showDictionaryKeyboard());
        }
    }

    @Override
    protected NoStory getStory() {
        return null;
    }

    private InlineKeyboardMarkup showDictionaryKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.IN_DETAIL))
                                .callbackData(Callback.DETAILED_DICTIONARY.getValue())
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
                                .callbackData(Callback.CHANGE_DIRECTION.getValue())
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

    private InlineKeyboardMarkup emptyDictionaryKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.ALTER_DICTIONARY))
                                .callbackData(Callback.ALTER_DICTIONARY.getValue())
                                .build()
                ))
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
