package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.FeedbackStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
public class FeedbackCommandProcessor extends CommandProcessor<FeedbackStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    public FeedbackCommandProcessor(
            Analytics analytics,
            UserService userService,
            SenderService senderService,
            UserRepository userRepository,
            NotifierService notifierService,
            MainMenuCommandProcessor mainMenuCommandProcessor) {
        super(analytics, userService, senderService, userRepository, notifierService);
        this.mainMenuCommandProcessor = mainMenuCommandProcessor;
    }

    @Override
    protected void processRequest() {
        FeedbackStory story = getStory();

        if (story == null) {
            UserContext.getUser().setFeedbackStory(new FeedbackStory());
            UserContext.getUser().getFeedbackStory().setLeavingFeedback(false);

            StringBuilder response = new StringBuilder(UserContext.getText(Text.CHAT_WORDS));
            if (buildProperties != null) {
                response.append("\n");
                response.append(UserContext.getText(Text.VERSION));
                response.append(" ");
                response.append(buildProperties.getVersion());
            }
            response.append("\n");
            response.append(UserContext.getText(Text.ABOUT));
            response.append("\n\n");
            response.append(UserContext.getText(Text.YANDEX_TERM));
            response.append("\n\n");
            response.append(UserContext.getText(Text.CONTACT_ME));

            analytics.log(UserEvent.OPEN_FEEDBACK);
            senderService.updateMessage(response.toString(), getFeedbackKeyboard());
        } else {
            if (UserContext.getRequest().equals(UserContext.getText(Text.EXIT))) {
                UserContext.getUser().clearStories();
                mainMenuCommandProcessor.processRequest();
                return;
            }

            if (UserContext.getRequest().equals(UserContext.getText(Text.LEAVE_REVIEW))) {
                UserContext.getUser().getFeedbackStory().setLeavingFeedback(true);
                analytics.log(UserEvent.LEAVING_FEEDBACK);
                senderService.updateMessage(Text.ENTER_FEEDBACK, null);
                return;
            }

            if (Boolean.TRUE.equals(story.getLeavingFeedback())) {
                UserContext.getUser().clearStories();
                analytics.log(UserEvent.NEW_FEEDBACK);
                analytics.feedback(UserContext.getRequest());
                senderService.sendMessage(Text.THANK_YOU_FOR_FEEDBACK, null);
                mainMenuCommandProcessor.processRequest();
            }
        }
    }

    @Override
    protected FeedbackStory getStory() {
        return UserContext.getUser().getFeedbackStory();
    }

    private InlineKeyboardMarkup getFeedbackKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_FEEDBACK))
                                .callbackData(UserContext.getText(Text.LEAVE_REVIEW))
                                .build()
                ))
                .keyboardRow(Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(UserContext.getText(Text.MENU_BUTTON))
                                .callbackData(UserContext.getText(Text.EXIT))
                                .build()
                ))
                .build();
    }
}
