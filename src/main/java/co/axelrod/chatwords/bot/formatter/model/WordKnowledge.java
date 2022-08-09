package co.axelrod.chatwords.bot.formatter.model;

import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import co.axelrod.chatwords.storage.UserDictionary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WordKnowledge {
    NEW("\uD83C\uDD95"),
    ONE("1️⃣"),
    TWO("2️⃣"),
    DONE("✅"),
    ;

    private final String emoji;

    public static WordKnowledge getEmojiByQuizCount(Integer quizCount) {
        if (quizCount == null || quizCount < 1) {
            return NEW;
        }

        if (quizCount == 1) {
            return ONE;
        }

        if (quizCount == 2) {
            return TWO;
        }

        if (quizCount >= UserDictionary.LEARNED_THRESHOLD_QUIZ_COUNT) {
            return DONE;
        }

        throw new ChatWordsRuntimeException("Invalid quiz or knowledge count");
    }
}
