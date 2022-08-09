package co.axelrod.chatwords.bot.command.message.dictionary;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.NoStory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ShowDictionaryAbstractCommandProcessor extends CommandProcessor<NoStory> {
    protected ShowDictionaryAbstractCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
    }

    protected List<InlineKeyboardButton> getPagingAndQuizKeyboardRow() {
        InlineKeyboardButton quizButton = InlineKeyboardButton.builder()
                .text(UserContext.getText(Text.START_QUIZ))
                .callbackData(Callback.QUIZ.getValue())
                .build();

        boolean hasLeftButton;
        boolean hasRightButton;

        int currentPage = UserContext.getUser().getPaging().getCurrentOffset() / UserContext.getUser().getPaging().getElementsOnPage();
        int totalPages = UserContext.getUser().getCurrentDictionary().getFilteredWords().size() / UserContext.getUser().getPaging().getElementsOnPage();
        if (totalPages * UserContext.getUser().getPaging().getElementsOnPage() < UserContext.getUser().getCurrentDictionary().getFilteredWords().size()) {
            totalPages++;
        }

        hasLeftButton = currentPage != 0;
        hasRightButton = currentPage < totalPages - 1;

        InlineKeyboardButton leftButton = InlineKeyboardButton.builder()
                .text(UserContext.getText(Text.PREVIOUS_PAGE))
                .callbackData(Callback.PREVIOUS_PAGE.getValue())
                .build();

        InlineKeyboardButton rightButton = InlineKeyboardButton.builder()
                .text(UserContext.getText(Text.NEXT_PAGE))
                .callbackData(Callback.NEXT_PAGE.getValue())
                .build();

        if (hasLeftButton && hasRightButton) {
            return Arrays.asList(leftButton, quizButton, rightButton);
        }

        if (hasLeftButton) {
            return Arrays.asList(leftButton, quizButton);
        }
        if (hasRightButton) {
            return Arrays.asList(quizButton, rightButton);
        }

        return Collections.singletonList(quizButton);
    }

    protected List<InlineKeyboardButton> getShowHideLearnedKeyboardRow() {
        InlineKeyboardButton button;

        if (UserContext.getUser().getShowLearned() == null || UserContext.getUser().getShowLearned()) {
            button = InlineKeyboardButton.builder()
                    .text(UserContext.getText(Text.HIDE_LEARNED))
                    .callbackData(Callback.HIDE_LEARNED.getValue())
                    .build();
        } else {
            button = InlineKeyboardButton.builder()
                    .text(UserContext.getText(Text.SHOW_LEARNED))
                    .callbackData(Callback.SHOW_LEARNED.getValue())
                    .build();
        }

        return Collections.singletonList(button);
    }

    protected void processShowHideLearned() {
        if (UserContext.getRequest().equals(Callback.SHOW_LEARNED.getValue())) {
            analytics.log(UserEvent.SHOW_LEARNED_WORDS_IN_DICTIONARY);
            UserContext.setCustomCallbackAlertText(UserContext.getText(Text.SHOWING_LEARNED_WORDS));
            UserContext.getUser().setShowLearned(true);
        }

        if (UserContext.getRequest().equals(Callback.HIDE_LEARNED.getValue())) {
            analytics.log(UserEvent.HIDE_LEARNED_WORDS_IN_DICTIONARY);
            UserContext.setCustomCallbackAlertText(UserContext.getText(Text.HIDING_LEARNED_WORDS));
            UserContext.getUser().setShowLearned(false);
        }
    }

    @Override
    protected NoStory getStory() {
        return null;
    }
}
