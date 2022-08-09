package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.sorting.Sort;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class UserDictionary {
    public static final int LEARNED_THRESHOLD_QUIZ_COUNT = 3;

    private String name;

    private Language sourceLanguage;
    private Language targetLanguage;

    private List<UserWord> words;

    private Sort sort;

    private Boolean notificationsOn;
    private String notificationsTime;

    private static final Random random = new SecureRandom();

    public UserDictionary(String name, Language sourceLanguage, Language targetLanguage) {
        this.name = name;
        this.words = new ArrayList<>();
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sort = Sort.OLD_TO_NEW;
        this.notificationsOn = false;
        this.notificationsTime = null;
    }

    public Sort getSort() {
        if (this.sort == null) {
            this.sort = Sort.OLD_TO_NEW;
        }

        return sort;
    }

    public void reverseSort() {
        if (getSort().equals(Sort.OLD_TO_NEW)) {
            sort = Sort.NEW_TO_OLD;
        } else {
            sort = Sort.OLD_TO_NEW;
        }
        Collections.reverse(words);
    }

    public List<UserWord> getFilteredWords() {
        return getWords().stream().filter(filterLearnedWords()).collect(Collectors.toList());
    }

    private Predicate<UserWord> filterLearnedWords() {
        return word -> {
            if (UserContext.getUser().getShowLearned() == null || UserContext.getUser().getShowLearned()) {
                return true;
            } else {
                return !word.isLearned();
            }
        };
    }

    public UserWord getRandomWord() {
        if (words.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(words.size());
        return words.get(randomIndex);
    }

    public UserWord getRandomWord(UserWord wordToExclude) {
        int attempts = 100;

        do {
            int randomIndex = random.nextInt(words.size());
            if (!words.get(randomIndex).getWord().getId().equals(wordToExclude.getWord().getId())) {
                return words.get(randomIndex);
            }
        } while (attempts-- > 0);

        return wordToExclude;
    }

    public Integer getLearnedWordsCount(Integer threshold) {
        return (int) words.stream().filter(userWord -> userWord.getQuizCount() >= threshold).count();
    }

    public Integer getLearnedWordsCount() {
        return getLearnedWordsCount(LEARNED_THRESHOLD_QUIZ_COUNT);
    }

    public String getLearnedPercent() {
        return formatLearnedPercent(getLearnedPercentRaw());
    }

    // TODO Перестать считать настолько точно, убрать проценты
    public Float getLearnedPercentRaw() {
        float totalQuizCount = words.stream()
                .map(UserWord::getQuizCount)
                .map(quizCount -> quizCount > LEARNED_THRESHOLD_QUIZ_COUNT ? LEARNED_THRESHOLD_QUIZ_COUNT : quizCount)
                .reduce(0, Integer::sum);

        float score = totalQuizCount / ((float) words.size() * LEARNED_THRESHOLD_QUIZ_COUNT);
        score *= 100;

        return score;
    }

    public static String formatLearnedPercent(Float score) {
        return String.format("%,.2f", score) + "%";
    }

    public void turnOffNotification() {
        this.notificationsOn = false;
        this.notificationsTime = null;
    }
}
