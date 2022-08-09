package co.axelrod.chatwords.storage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Activity {
    private int wordsTranslated;
    private int sentencesTranslated;
    private int wordsAdded;
    private int quizzesPassed;
    private int wordsLearned;

    public Activity() {
        this.wordsTranslated = 0;
        this.sentencesTranslated = 0;
        this.wordsAdded = 0;
        this.quizzesPassed = 0;
        this.wordsLearned = 0;
    }

    public void wordTranslated() {
        this.wordsTranslated++;
    }

    public void sentenceTranslated() {
        this.sentencesTranslated++;
    }

    public void wordAdded() {
        this.wordsAdded++;
    }

    public void quizPassed() {
        this.quizzesPassed++;
    }

    public void wordLearned() {
        this.wordsLearned++;
    }
}
