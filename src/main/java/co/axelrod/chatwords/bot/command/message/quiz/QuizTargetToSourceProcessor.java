package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.QuizStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuizTargetToSourceProcessor extends AbstractQuizModeProcessor {
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected QuizTargetToSourceProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            QuizService quizService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService, quizService);
    }

    @Override
    protected void processRequest() {
        // Слова еще остались, отправляем следующее слово
        sendNextWord(
                UserContext.getUser().getCurrentDictionary().getTargetLanguage(),
                UserContext.getUser().getCurrentDictionary().getSourceLanguage()
        );
    }

//    protected InlineKeyboardMarkup finishedQuizKeyboard() {
//        return InlineKeyboardMarkup.builder()
//                .keyboardRow(Collections.singletonList(
//                        InlineKeyboardButton.builder()
//                                .text(UserContext.getText(Text.FINISH_QUIZ))
//                                .callbackData(Callback.SHOW_DICTIONARY.getValue())
//                                .build()
//                ))
//                .keyboardRow(Collections.singletonList(
//                        InlineKeyboardButton.builder()
//                                .text(UserContext.getText(Text.CONTINUE_QUIZ))
//                                .callbackData(Callback.QUIZ.getValue())
//                                .build()
//                ))
//                .build();
//    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }
}
