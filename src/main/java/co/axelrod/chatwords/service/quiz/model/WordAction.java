package co.axelrod.chatwords.service.quiz.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WordAction {
    ADD_WORD_TO_QUIZ("addWordToQuiz"),
    SKIP_WORD("skipWord");

    private final String value;

    public static WordAction fromValue(String text) {
        for (WordAction wordAction : WordAction.values()) {
            if (wordAction.value.equals(text)) {
                return wordAction;
            }
        }
        return null;
    }
}