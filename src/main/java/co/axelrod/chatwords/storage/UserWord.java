package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.service.quiz.model.QuizMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode
@Document
public class UserWord {
    private Integer quizCount;

    private Integer sourceToTargetQuizCount;
    private Integer targetToSourceQuizCount;
    private Integer sourceTypingQuizCount;
    private Integer targetTypingQuizCount;

    @DBRef(lazy = true)
    private Word word;

    public UserWord(Word word) {
        this.quizCount = 0;
        this.sourceToTargetQuizCount = 0;
        this.targetToSourceQuizCount = 0;
        this.sourceTypingQuizCount = 0;
        this.targetTypingQuizCount = 0;
        this.word = word;
    }

    public boolean isLearned() {
        if (quizCount == null) {
            return false;
        }

        return quizCount >= UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT;
    }

    public boolean wordIsLearned(QuizMode quizMode, Integer threshold) {
        switch (quizMode) {
            case SOURCE_TO_TARGET:
                return sourceToTargetQuizCount > threshold;

            case TARGET_TO_SOURCE:
                return targetToSourceQuizCount > threshold;

            case SOURCE_TYPING:
                return sourceTypingQuizCount > threshold;

            case TARGET_TYPING:
                return targetTypingQuizCount > threshold;
        }

        return true;
    }
}
