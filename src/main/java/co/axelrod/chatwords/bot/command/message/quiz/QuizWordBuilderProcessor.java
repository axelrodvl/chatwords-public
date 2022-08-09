package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.service.quiz.QuizService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.UserWord;
import co.axelrod.chatwords.storage.story.QuizStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuizWordBuilderProcessor extends AbstractQuizModeProcessor {
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected QuizWordBuilderProcessor(
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
    protected void processRequest() {
        if (getStory().getQuizCompleted()) {
            quizCommandProcessor.processRequest();
        }

        // Текущего слова нет - устанавливаем
        if (getStory().getCurrentWord() == null) {
            getStory().setCurrentWord(quizService.getNextQuizWord());
        }

        setWordToBuild();

        UserWord userWord = getStory().getCurrentWord();
        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

        // Получили букву
        if (UserContext.getRequest().length() == 1) {
            if (getStory().getWordBuilt() == null) {
                getStory().setWordBuilt("");
            }

            Character character = UserContext.getRequest().charAt(0);
            if (character.equals(getStory().getWordToBuild().charAt(getStory().getWordBuilt().length()))) {

                int lastIndexOfCharacter = getStory().getWordBuilder().lastIndexOf(character);
                getStory().getWordBuilder().remove(lastIndexOfCharacter);

                getStory().setWordBuilt(getStory().getWordBuilt() + character);
            }

            // Слово собрано - добавляем слово к изученным
            if (getStory().getWordToBuild().equals(getStory().getWordBuilt())) {
                userWord.setSourceTypingQuizCount(userWord.getSourceTypingQuizCount() + 1);
                userWord.setQuizCount(userWord.getQuizCount() + 1);
                if (userWord.isLearned()) {
                    UserContext.getUser().getCurrentDayActivity().wordLearned();
                }
                getStory().setWordsCorrect(getStory().getWordsCorrect() + 1);
                userWord = quizService.getNextQuizWord();
                getStory().setCurrentWord(userWord);

                // Если все слова кончились, завершаем квиз
                if (getStory().getCurrentWord() == null) {
                    getStory().setQuizCompleted(true);
                    quizCommandProcessor.processRequest();
                    return;
                }

                getStory().setWordBuilder(null);
                setWordToBuild();
                getStory().setWordBuilt("");

                // TODO Временный грязный фикс
                String lastWordsCount = (getStory().getCurrentQuizWords().size() + 1) + "/" + (getStory().getQuizWords().size() + 1);

                String messageText =
                        UserContext.getText(Text.QUIZ_ANSWER_IS_CORRECT) + " " + UserContext.getText(Text.QUIZ_WORDS_LEFT) + " " + lastWordsCount
                                + "\n"
                                + UserContext.getText(Text.NEXT_QUIZ_WORD)
                                + "\n"
                                + source.getEmoji()
                                + " "
                                + userWord.getWord().getTranslation(source)
                                + "\n"
                                + target.getEmoji()
                                + " "
                                + (getStory().getWordBuilt() == null ? "" : getStory().getWordBuilt());
                senderService.updateMessage(messageText, wordBuilderKeyboard());
                return;
            }
        }

        String messageText = "";

        if (getStory().getQuizWords().size() == getStory().getCurrentQuizWords().size()) {
            messageText += UserContext.getText(Text.NEXT_QUIZ_WORD) + "\n";
        } else {
            // TODO Временный грязный фикс. Умножить на порядковый номер квиза (если это третий по счету для текущего квиза, умножаем на 3)
            String lastWordsCount = (getStory().getCurrentQuizWords().size() + 1) + "/" + (getStory().getQuizWords().size());

            messageText += UserContext.getText(Text.QUIZ_ANSWER_IS_CORRECT) + " " + UserContext.getText(Text.QUIZ_WORDS_LEFT) + " " + lastWordsCount + "\n"
                    + UserContext.getText(Text.NEXT_QUIZ_WORD);
        }

        messageText += "\n"
                + source.getEmoji()
                + " "
                + userWord.getWord().getTranslation(source)
                + "\n"
                + target.getEmoji()
                + " "
                + (getStory().getWordBuilt() == null ? "" : getStory().getWordBuilt());
        senderService.updateMessage(messageText, wordBuilderKeyboard());
    }

    private void setWordToBuild() {
        UserWord userWord = getStory().getCurrentWord();
        Language source = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
        Language target = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

        String wordToBuild = userWord.getWord().getTranslation(target);

        if (getStory().getWordBuilder() == null) {
            getStory().setWordToBuild(wordToBuild);

            char[] letters = wordToBuild.toCharArray();
            List<Character> uniqueCharacters = new LinkedList<>();
            for (char letter : letters) {
                uniqueCharacters.add(letter);
            }
            Collections.shuffle(uniqueCharacters);
            getStory().setWordBuilder(uniqueCharacters);
        }
    }

    private InlineKeyboardMarkup wordBuilderKeyboard() {
        Set<Character> characters = new LinkedHashSet<>(getStory().getWordBuilder());

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        int rowsCount = characters.size() / 8;
        if (characters.size() % 8 > 0) {
            rowsCount++;
        }

        for (int i = 0; i < rowsCount; i++) {
            builder.keyboardRow(
                    characters
                            .stream()
                            .skip(i * 8L)
                            .limit(8L)
                            .map(character -> InlineKeyboardButton.builder()
                                    .text("" + character.toString() + "")
                                    .callbackData(character.toString())
                                    .build())
                            .collect(Collectors.toList())
            );
        }

        return builder.build();
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }
}
