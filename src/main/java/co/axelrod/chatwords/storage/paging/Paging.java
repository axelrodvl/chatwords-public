package co.axelrod.chatwords.storage.paging;

import co.axelrod.chatwords.bot.formatter.model.WordKnowledge;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import co.axelrod.chatwords.storage.UserWord;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
public class Paging {
    private Integer elementsOnPage;
    private PagingScreen pagingScreen;
    private Integer currentOffset;

    public Paging(PagingScreen pagingScreen, Integer elementsOnPage) {
        this.pagingScreen = pagingScreen;
        this.currentOffset = 0;
        this.elementsOnPage = elementsOnPage;
    }

    public List<String> previousPage(List<UserWord> words) {
        if (currentOffset.equals(elementsOnPage)) {
            this.currentOffset = 0;
        } else {
            this.currentOffset -= elementsOnPage;
        }
        return getPage(words);
    }

    public List<String> nextPage(List<UserWord> words) {
        this.currentOffset = Math.min(this.currentOffset + elementsOnPage, words.size());
        return getPage(words);
    }

    @RequiredArgsConstructor
    @Getter
    static class UserWordWithIndex {
        private final int index;
        private final UserWord userWord;
    }

    public List<String> getPage(List<UserWord> words) {
        return IntStream.range(0, words.size())
                .skip(currentOffset)
                .limit(elementsOnPage)
                .mapToObj(index -> new UserWordWithIndex(index + 1, words.get(index)))
                .map(getFormatter())
                .collect(Collectors.toList());
    }

    public Function<UserWordWithIndex, String> getFormatter() {
        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

        switch (pagingScreen) {
            case DETAILED_DICTIONARY:
                return userWordWithIndex ->
                        WordKnowledge.getEmojiByQuizCount(userWordWithIndex.getUserWord().getQuizCount()).getEmoji()
                                + " " + userWordWithIndex.index + ". "
                                + userWordWithIndex.getUserWord().getWord().getTranslation(source)
                                + " • "
                                + userWordWithIndex.getUserWord().getWord().getTranslation(target)
                                + "\n\n"
                                + userWordWithIndex.getUserWord().getWord().getFullTranslation(source, target);
            case DICTIONARY:
                return userWordWithIndex ->
                        WordKnowledge.getEmojiByQuizCount(userWordWithIndex.getUserWord().getQuizCount()).getEmoji()
                                + " " + userWordWithIndex.index + ". "
                                + userWordWithIndex.getUserWord().getWord().getTranslation(source)
                                + " • "
                                + userWordWithIndex.getUserWord().getWord().getTranslation(target);
        }

        throw new ChatWordsRuntimeException("Invalid PagingScreen");
    }
}
