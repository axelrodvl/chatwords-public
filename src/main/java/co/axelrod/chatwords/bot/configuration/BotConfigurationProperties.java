package co.axelrod.chatwords.bot.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Accessors(chain = true)
@Component
@ConfigurationProperties("bot")
public class BotConfigurationProperties {
    private Boolean maintenanceMode;

    private String botPath;

    private String name;
    private String token;

    private String infoName;
    private String infoToken;
    private String infoChatId;
    private String reviewChatId;
}
