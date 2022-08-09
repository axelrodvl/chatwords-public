package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.command.CommandProcessor;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;

public abstract class AbstractQuizModeProcessor extends CommandProcessor<QuizStory> {
    protected final QuizService quizService;

    protected AbstractQuizModeProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            QuizService quizService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.quizService = quizService;
    }

    protected void sendNextWord(Language source, Language target) {

        // Первое слово квиза
        if (getStory().getQuizWords().size() == getStory().getCurrentQuizWords().size()) {
            getStory().setCurrentWord(quizService.getNextQuizWord());
            getStory().setLastWord(getStory().getCurrentWord());
            senderService.updateMessage(
                    getStartQuizMessage(getStory().getCurrentWord(), source),
                    quizVariantsKeyboard(quizService.getQuizResponseVariants(getStory().getCurrentWord(), target))
            );
            return;
        }

        getStory().setCurrentWord(quizService.getNextQuizWord());

        // Пришел ответ на слово
        boolean answerIsCorrect = UserContext.getRequest().equalsIgnoreCase(
                getStory().getLastWord().getWord().getTranslation(
                        UserContext.getUser().getCurrentDictionary().getTargetLanguage()
                )
        );

        String response;
        if (answerIsCorrect) {
            // Правильный ответ
            getStory().setWordsCorrect(getStory().getWordsCorrect() + 1);
            UserWord wordToUpdate = UserContext.getUser().getCurrentDictionary().getWords()
                    .stream()
                    .filter(uw -> uw.getWord().getId().equals(getStory().getLastWord().getWord().getId())).findFirst()
                    .get();
            wordToUpdate.setQuizCount(wordToUpdate.getQuizCount() + 1);
            if (wordToUpdate.isLearned()) {
                UserContext.getUser().getCurrentDayActivity().wordLearned();
            }

            response = UserContext.getText(Text.QUIZ_ANSWER_IS_CORRECT) + " " + UserContext.getText(Text.QUIZ_WORDS_LEFT) + " " + getLeftWordsCount() + "\n";
        } else {
            // Неправильный ответ
            UserWord wordToUpdate = UserContext.getUser().getCurrentDictionary().getWords()
                    .stream()
                    .filter(uw -> uw.getWord().getId().equals(getStory().getLastWord().getWord().getId())).findFirst()
                    .get();
            wordToUpdate.setQuizCount(wordToUpdate.getQuizCount() - 1);
            response = UserContext.getText(Text.QUIZ_ANSWER_IS_INCORRECT) + " " + UserContext.getText(Text.QUIZ_WORDS_LEFT) + " " + getLeftWordsCount()
                    + "\n" + UserContext.getText(Text.QUIZ_CORRECT_ANSWER) + " "
                    + getStory().getLastWord().getWord().getTranslation(
                    UserContext.getUser().getCurrentDictionary().getTargetLanguage()
            );
        }

        // Если все слова кончились, завершаем квиз
        if (getStory().getCurrentWord() == null) {
            getStory().setQuizCompleted(true);
            return;
        }

        // Получаем следующее слово
        response += "\n" + UserContext.getText(Text.NEXT_QUIZ_WORD) + "\n"
                + source.getEmoji() + " <b>" + getStory().getCurrentWord().getWord().getTranslation(UserContext.getUser().getCurrentDictionary().getSourceLanguage()) + "</b>";

        getStory().setLastWord(getStory().getCurrentWord());
        senderService.updateMessage(response, quizVariantsKeyboard(quizService.getQuizResponseVariants(getStory().getCurrentWord(), target)));
    }

    private String getStartQuizMessage(UserWord userWord, Language source) {
        StringBuilder startQuizMessage = new StringBuilder();
        startQuizMessage.append(UserContext.getText(Text.BEGINNING_QUIZ));
        startQuizMessage.append("\n\n");
        startQuizMessage.append(UserContext.getText(Text.FIRST_QUIZ_WORD));
        startQuizMessage.append("\n");
        startQuizMessage.append(source.getEmoji());
        startQuizMessage.append(" <b>");
        startQuizMessage.append(userWord.getWord().getTranslation(source));
        startQuizMessage.append("</b>");
        return startQuizMessage.toString();
    }

    public String getLeftWordsCount() {
        return (getStory().getCurrentQuizWords().size() + 1) + "/" + getStory().getQuizWords().size();
    }

    protected InlineKeyboardMarkup quizVariantsKeyboard(List<String> words) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(InlineKeyboardButton.builder()
                        .text(words.get(0))
                        .callbackData(words.get(0))
                        .build()))
                .keyboardRow(Collections.singletonList(InlineKeyboardButton.builder()
                        .text(words.get(1))
                        .callbackData(words.get(1))
                        .build()))
                .keyboardRow(Collections.singletonList(InlineKeyboardButton.builder()
                        .text(words.get(2))
                        .callbackData(words.get(2))
                        .build()))
                .keyboardRow(Collections.singletonList(InlineKeyboardButton.builder()
                        .text(words.get(3))
                        .callbackData(words.get(3))
                        .build()))
                .build();
    }
}
