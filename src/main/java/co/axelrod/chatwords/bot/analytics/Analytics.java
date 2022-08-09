package co.axelrod.chatwords.bot.analytics;

import co.axelrod.chatwords.bot.ChatWordsInfoBot;
import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.storage.User;
import com.amplitude.Amplitude;
import com.amplitude.Event;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class Analytics {
    private final BotConfigurationProperties properties;
    private final ChatWordsInfoBot chatWordsInfoBot;
    private final Amplitude amplitude;

    public Analytics(
            BotConfigurationProperties botConfigurationProperties,
            @Qualifier("chatWordsInfoBot") ChatWordsInfoBot chatWordsInfoBot,
            Amplitude amplitude
    ) {
        this.properties = botConfigurationProperties;
        this.chatWordsInfoBot = chatWordsInfoBot;
        this.amplitude = amplitude;
    }

    @SneakyThrows
    public void logCampaign(String campaignName) {
        String campaign = "Campaign: " + campaignName;
        amplitude.logEvent(new Event(campaign, UserContext.getUser().getName()));
        chatWordsInfoBot.executeAsync(new SendMessage(properties.getInfoChatId(),
                UserContext.getUser().getName() + ":\n" + campaign)
        );
    }

    public void log(UserEvent userEvent) {
        log(UserContext.getUser(), userEvent);
    }

    @SneakyThrows
    public void log(User user, UserEvent userEvent) {
        amplitude.logEvent(new Event(userEvent.getValue(), user.getName()));
        chatWordsInfoBot.executeAsync(new SendMessage(properties.getInfoChatId(), user.getName() + ":\n" + userEvent.getValue()));
    }

    @SneakyThrows
    public void feedback(String message) {
        chatWordsInfoBot.executeAsync(new SendMessage(properties.getReviewChatId(), "✉️ Feedback from " + UserContext.getUser().getName() + ":\n" + message));
    }
}
