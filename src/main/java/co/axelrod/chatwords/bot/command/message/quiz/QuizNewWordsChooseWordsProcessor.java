package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.service.quiz.model.QuizMode;
import co.axelrod.chatwords.storage.UserDictionary;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class QuizNewWordsChooseWordsProcessor extends AbstractChooseWordsProcessor {
    protected QuizNewWordsChooseWordsProcessor(
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
    protected void setAvailableQuizWords() {
        // Добавляем самые неизученные слова в квиз
        getStory().setAvailableWords(quizService.getAllWordsForQuiz(QuizMode.SOURCE_TO_TARGET));
        getStory().setAvailableWords(
                getStory().getAvailableWords().stream()
                        .filter(userWord -> userWord.getQuizCount() < UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT)
                        .sorted(Comparator.comparing(UserWord::getQuizCount))
                        .collect(Collectors.toList())
        );
    }
}
