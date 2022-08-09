package co.axelrod.chatwords.storage.story;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SentenceToDictionaryStory extends UserStory {
    private Integer currentOffset;
    private List<WordFromSentence> words;

    public SentenceToDictionaryStory() {
        this.currentOffset = 0;
        this.words = new ArrayList<>();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class WordFromSentence {
        private String word;
        private Boolean addedToDictionary;
    }
}
