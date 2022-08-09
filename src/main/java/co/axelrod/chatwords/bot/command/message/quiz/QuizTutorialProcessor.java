package co.axelrod.chatwords.bot.command.message.quiz;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.Callback;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.model.callback.FinishQuizTutorialCallback;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.bot.util.CallbackUtil;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.Onboarding;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.QuizStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
public class QuizTutorialProcessor extends CommandProcessor<QuizStory> {
    @Autowired
    private QuizCommandProcessor quizCommandProcessor;

    protected QuizTutorialProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService
    ) {
        super(analytics, userService, senderService, userRepository, notifierService);
    }

    @Override
    protected void processRequest() {
        if (UserContext.getUser().getOnboarding() == null) {
            UserContext.getUser().setOnboarding(new Onboarding());
        }

        Callback callback = CallbackUtil.getCallback(UserContext.getRequest());
        if (callback != null && callback.equals(Callback.FINISH_QUIZ_TUTORIAL)) {
            UserContext.getUser().getOnboarding().setShowQuizTutorial(false);
            analytics.log(UserEvent.FINISHED_QUIZ_TUTORIAL);
            quizCommandProcessor.processRequest();
            return;
        }

        if (getStory().getTutorialMessageCount() == 0) {
            senderService.sendMessage(Text.QUIZ_TUTORIAL_PART_1, startQuizKeyboard());
        }
    }

    @Override
    protected QuizStory getStory() {
        return UserContext.getUser().getQuizStory();
    }

    private InlineKeyboardMarkup startQuizKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(UserContext.getText(Text.QUIZ_TUTORIAL_START_BUTTON))
                                        .callbackData(FinishQuizTutorialCallback.builder().build().code())
                                        .build()
                        )
                )
                .build();
    }
}
