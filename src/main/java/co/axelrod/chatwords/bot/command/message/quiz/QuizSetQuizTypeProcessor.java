package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.command.message.dictionary.ShowDictionaryCommandProcessor;
import co.axelrod.chatwords.bot.formatter.DictionaryFormatter;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.QuizTypeCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.model.QuizType;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.QuizStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
public class QuizSetQuizTypeProcessor extends CommandProcessor<QuizStory> {
    private final ShowDictionaryCommandProcessor showDictionaryCommandProcessor;

    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected QuizSetQuizTypeProcessor(
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
        if (UserContext.getRequest().equals(UserContext.getText(Text.EXIT))) {
            UserContext.getUser().clearStories();
            showDictionaryCommandProcessor.processRequest();
            return;
        }

        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.equals(Callback.QUIZ_TYPE)) {
            QuizTypeCallback quizTypeCallback = new QuizTypeCallback(UserContext.getRequest());
            getStory().setQuizType(QuizType.valueOf(quizTypeCallback.getType()));

            quizCommandProcessor.processRequest();
            return;
        }

        senderService.updateMessage(
                DictionaryFormatter.getDictionary(UserContext.getUser().getCurrentDictionary())
                        + "\n"
                        + UserContext.getText(Text.QUIZ_TYPE),
                quizTypeKeyboard()
        );
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }

    private InlineKeyboardMarkup quizTypeKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        boolean dictionaryNotLearnt = !UserContext.getUser().getCurrentDictionary().getLearnedWordsCount()
                .equals(UserContext.getUser().getCurrentDictionary().getWords().size());

        if (dictionaryNotLearnt) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.QUIZ_NEW_WORDS_BUTTON))
                                    .callbackData(QuizTypeCallback.builder()
                                            .type(QuizType.NEW.name())
                                            .build().code())
                                    .build()
                    )
            );
        }

        boolean hasLearnedWords = UserContext.getUser().getCurrentDictionary().getWords()
                .stream()
                .anyMatch(userWord -> userWord.getQuizCount() > 0);

        if (hasLearnedWords) {
            builder.keyboardRow(
                    Collections.singletonList(
                            InlineKeyboardButton.builder()
                                    .text(UserContext.getText(Text.QUIZ_REPEAT_WORDS_BUTTON))
                                    .callbackData(QuizTypeCallback.builder()
                                            .type(QuizType.REPEAT.name())
                                            .build().code())
                                    .build()
                    )
            );
        }

        builder.keyboardRow(
                Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.QUIZ_RANDOM_WORDS_BUTTON))
                                .callbackData(QuizTypeCallback.builder()
                                        .type(QuizType.RANDOM.name())
                                        .build().code())
                                .build()
                )
        );

        builder.keyboardRow(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text(UserContext.getText(Text.EXIT))
                        .callbackData(UserContext.getText(Text.EXIT))
                        .build()
        ));

        return builder.build();
    }
}
