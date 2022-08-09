package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuizRepeatWordsChooseWordsProcessor extends AbstractChooseWordsProcessor {
    protected QuizRepeatWordsChooseWordsProcessor(
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
        // Добавляем самые изученные слова в Квиз
        List<UserWord> knownWords = UserContext.getUser().getCurrentDictionary().getWords().stream()
                .filter(userWord -> userWord.getQuizCount() > 0)
                .sorted(Comparator.comparing(UserWord::getQuizCount))
                .limit(quizService.getQuizLength())
                .collect(Collectors.toCollection(LinkedList::new));
        getStory().setAvailableWords(knownWords);
    }
}
