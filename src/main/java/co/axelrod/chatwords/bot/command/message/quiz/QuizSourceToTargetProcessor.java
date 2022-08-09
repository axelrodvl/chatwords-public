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
public class QuizSourceToTargetProcessor extends AbstractQuizModeProcessor {
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected QuizSourceToTargetProcessor(
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
        sendNextWord(
                UserContext.getUser().getCurrentDictionary().getSourceLanguage(),
                UserContext.getUser().getCurrentDictionary().getTargetLanguage()
        );

        if (getStory().getQuizCompleted()) {
            quizCommandProcessor.processRequest();
        }
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }
}
