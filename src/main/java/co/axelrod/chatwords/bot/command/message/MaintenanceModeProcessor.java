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
import co.axelrod.chatwords.storage.story.NoStory;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceModeProcessor extends CommandProcessor<NoStory> {
    public MaintenanceModeProcessor(
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
        analytics.log(UserEvent.CALL_WHEN_IN_MAINTENANCE_MODE);
        senderService.sendMessage(UserContext.getText(Text.MAINTENANCE_MODE));
    }

    @Override
    protected NoStory getStory() {
        return null;
    }
}
