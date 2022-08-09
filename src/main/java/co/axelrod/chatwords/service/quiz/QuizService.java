package co.axelrod.chatwords.service.quiz;

import co.axelrod.chatwords.bot.configuration.QuizConfigurationProperties;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.generator.WordGenerator;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import co.axelrod.chatwords.service.quiz.model.QuizMode;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.story.QuizStory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizService {
    private static final Random random = new SecureRandom();

    private final QuizConfigurationProperties properties;

    public void initializeQuiz() {
        QuizStory quizStory = new QuizStory();

        quizStory.setWordsCorrect(0);
        quizStory.setTutorialMessageCount(0);

        quizStory.setQuizReady(false);
        quizStory.setQuizCompleted(false);

        // Начинаем с первого типа (SOURCE_TO_TARGET)
        QuizMode defaultMode = QuizMode.SOURCE_TO_TARGET;
        quizStory.setQuizMode(defaultMode);

        UserContext.getUser().setQuizStory(quizStory);
    }

    // State
    public int getQuizLength() {
        int quizLength;
        if (UserContext.getUser().getUserSettings() == null || UserContext.getUser().getUserSettings().getQuizLength() == null) {
            quizLength = properties.getWordsCount();
        } else {
            quizLength = UserContext.getUser().getUserSettings().getQuizLength();
        }
        return quizLength;
    }

    public boolean quizUnavailable() {
        return UserContext.getUser().getCurrentDictionary().getWords() == null
                || UserContext.getUser().getCurrentDictionary().getWords().isEmpty();
    }

    public QuizMode getNextQuizType(QuizMode currentQuizMode) {
        int position = currentQuizMode.ordinal();
        if (QuizMode.values().length - 1 <= position + 1) {
            return QuizMode.values()[position + 1];
        } else {
            return QuizMode.values()[0];
        }
    }

    // Words for quiz
    public List<String> getQuizResponseVariants(UserWord userWord, Language language) {
        List<String> words = new ArrayList<>();
        words.add(userWord.getWord().getTranslation(language));

        for (int i = 0; i < properties.getVariants() - 1; i++) {
            if (random.nextBoolean()) {
                // Добавляем вариант ответа со случайным словом из текущего словаря
                String wordFromDictionary = UserContext.getUser().getCurrentDictionary().getRandomWord(userWord)
                        .getWord().getTranslation(language);

                if (words.contains(wordFromDictionary)) {
                    // Добавляем вариант ответа с полностью случайным словом
                    String randomWord = WordGenerator.getRandomWord(language);
                    while (words.contains(randomWord)) {
                        randomWord = WordGenerator.getRandomWord(language);
                    }
                    words.add(randomWord);
                } else {
                    words.add(wordFromDictionary);
                }
            } else {
                // Добавляем вариант ответа с полностью случайным словом
                String randomWord = WordGenerator.getRandomWord(language);
                while (words.contains(randomWord)) {
                    randomWord = WordGenerator.getRandomWord(language);
                }
                words.add(randomWord);
            }
        }

        Collections.shuffle(words);
        return words;
    }

    // Наполнение квиза (получение следующего доступного слова)
    public Optional<UserWord> getNextAvailableWordForQuiz() {
        if (UserContext.getUser().getQuizStory().getAvailableWords().size() > 0) {
            return Optional.of(UserContext.getUser().getQuizStory().getAvailableWords().get(0));
        } else {
            return Optional.empty();
        }
    }

    // Получение следующего слова для квиза
    public UserWord getNextQuizWord() {
        if (UserContext.getUser().getQuizStory().getCurrentQuizWords().isEmpty()) {
            return null;
        }

        UserWord userWord = UserContext.getUser().getQuizStory().getCurrentQuizWords().get(0);
        UserContext.getUser().getQuizStory().getCurrentQuizWords().remove(0);
        return userWord;
    }

    private Comparator<UserWord> getWordKnowledgeComparator(QuizMode quizMode) {
        Comparator<UserWord> comparator;

        // Выбираем критерий сравнения слов
        switch (quizMode) {
            case SOURCE_TO_TARGET:
                comparator = Comparator.comparing(UserWord::getSourceToTargetQuizCount);
                break;
            case TARGET_TO_SOURCE:
                comparator = Comparator.comparing(UserWord::getTargetToSourceQuizCount);
                break;
            case SOURCE_TYPING:
                comparator = Comparator.comparing(UserWord::getSourceTypingQuizCount);
                break;
            case TARGET_TYPING:
                comparator = Comparator.comparing(UserWord::getTargetTypingQuizCount);
                break;
            default:
                throw new ChatWordsRuntimeException("Invalid quiz type");
        }
        return comparator;
    }

    public List<UserWord> getAllWordsForQuiz(QuizMode quizMode) {
        Comparator<UserWord> comparator = getWordKnowledgeComparator(quizMode);

        // Добавляем самые неизученные слова в Квиз
        return UserContext.getUser().getCurrentDictionary().getWords().stream()
                .sorted(comparator)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public void removeWordFromAvailableWords(String wordId) {
        Optional<UserWord> userWord = UserContext.getUser().getQuizStory().getAvailableWords()
                .stream()
                .filter(uw -> uw.getWord().getId().equals(wordId))
                .findFirst();

        userWord.ifPresent(word -> UserContext.getUser().getQuizStory().getAvailableWords()
                .remove(word)
        );
    }

    public void addWordToQuiz(String wordId) {
        Optional<UserWord> userWord = UserContext.getUser().getQuizStory().getAvailableWords()
                .stream()
                .filter(uw -> uw.getWord().getId().equals(wordId))
                .findFirst();

        userWord.ifPresent(word -> UserContext.getUser().getQuizStory().getQuizWords()
                .add(word)
        );
    }

    // Добавляем в квиз уже изученные слова
    public void setAlreadyLearnedWordsForQuiz() {
        // Добавляем самые изученные слова в Квиз
        List<UserWord> knownWords = UserContext.getUser().getCurrentDictionary().getWords().stream()
                .filter(userWord -> userWord.getQuizCount() > 0)
                .sorted(Comparator.comparing(UserWord::getQuizCount))
                .limit(getQuizLength())
                .collect(Collectors.toCollection(LinkedList::new));

        UserContext.getUser().getQuizStory().setQuizWords(knownWords);
        UserContext.getUser().getQuizStory().setCurrentQuizWords(new ArrayList<>(knownWords));
    }

    // Добавляем в квиз случайные слова
    public void setRandomWordsForQuiz() {
        List<UserWord> userWords = UserContext.getUser().getCurrentDictionary().getWords();

        Set<Integer> randomIndexes = new HashSet<>();
        int wordCount = Math.min(getQuizLength(), userWords.size());
        while (randomIndexes.size() < wordCount) {
            randomIndexes.add(random.nextInt(wordCount));
        }

        List<UserWord> randomWords = new LinkedList<>();
        for (Integer randomIndex : randomIndexes) {
            randomWords.add(userWords.get(randomIndex));
        }

        UserContext.getUser().getQuizStory().setQuizWords(randomWords);
        UserContext.getUser().getQuizStory().setCurrentQuizWords(new ArrayList<>(randomWords));
    }

//    public void initializeQuiz(QuizMode quizMode) {
//        switch (quizMode) {
//            case SOURCE_TO_TARGET:
//                initializeSourceToTarget();
//                break;
//            case TARGET_TO_SOURCE:
//                initializeTargetToSource();
//                break;
//            case SOURCE_TYPING:
//                initializeSourceTyping();
//                break;
//            case TARGET_TYPING:
//                initializeTargetTyping();
//                break;
//        }
//    }
//
//    private void initializeSourceToTarget() {
//        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();
//
//        List<UserWord> getWordsForQuiz = getWordsForQuiz(QuizMode.SOURCE_TO_TARGET);
//    }
//
//    private void initializeTargetToSource() {
//        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();
//
//        List<UserWord> getWordsForQuiz = getWordsForQuiz(QuizMode.SOURCE_TO_TARGET);
//    }
//
//    private void initializeSourceTyping() {
//        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();
//
//        List<UserWord> getWordsForQuiz = getWordsForQuiz(QuizMode.SOURCE_TO_TARGET);
//    }
//
//    private void initializeTargetTyping() {
//        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
//        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();
//
//        List<UserWord> getWordsForQuiz = getWordsForQuiz(QuizMode.SOURCE_TO_TARGET);
//    }
}
