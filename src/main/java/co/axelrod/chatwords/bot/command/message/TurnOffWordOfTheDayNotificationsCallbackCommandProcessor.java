package co.axelrod.chatwords.bot.command.message;

import co.axelrod.chatwords.bot.analytics.Analytics;
import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.command.CommandProcessor;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.bot.service.SenderService;
import co.axelrod.chatwords.service.NotifierService;
import co.axelrod.chatwords.service.UserService;
import co.axelrod.chatwords.storage.UserRepository;
import co.axelrod.chatwords.storage.story.NoStory;
import org.springframework.stereotype.Component;

@Component
public class TurnOffWordOfTheDayNotificationsCallbackCommandProcessor extends CommandProcessor<NoStory> {
    private final MainMenuCommandProcessor mainMenuCommandProcessor;

    public TurnOffWordOfTheDayNotificationsCallbackCommandProcessor(
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
        UserContext.getUser().setTurnOffWordOfTheDay(true);
        analytics.log(UserEvent.TURN_OFF_WORD_OF_THE_DAY_REMINDER);
        mainMenuCommandProcessor.process();
    }


    @Override
    protected NoStory getStory() {
        return null;
    }
}
