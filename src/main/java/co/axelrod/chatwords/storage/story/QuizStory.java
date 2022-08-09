package co.axelrod.chatwords.storage.story;

import co.axelrod.chatwords.service.quiz.model.QuizMode;
import co.axelrod.chatwords.service.quiz.model.QuizType;
import co.axelrod.chatwords.storage.UserWord;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuizStory extends UserStory {
    private Integer wordsCorrect;

    private UserWord currentWord;
    private UserWord lastWord;

    private Boolean quizReady;
    private Boolean quizCompleted;

    // Наполнение словами (временное хранилище)
    private List<UserWord> availableWords;

    // Слова для всего квиза
    private List<UserWord> quizWords;

    // Слова для текущего типа квиза
    private List<UserWord> currentQuizWords;

    private QuizType quizType;
    private QuizMode quizMode;

    private Integer tutorialMessageCount;

    private String wordToBuild;
    private List<Character> wordBuilder;
    private String wordBuilt;

    public QuizStory() {
        this.quizWords = new ArrayList<>();
    }
}
