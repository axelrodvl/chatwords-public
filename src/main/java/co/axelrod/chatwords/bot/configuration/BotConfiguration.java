package co.axelrod.chatwords.bot.configuration;

import co.axelrod.chatwords.bot.ChatWordsBot;
import co.axelrod.chatwords.bot.ChatWordsInfoBot;
import co.axelrod.chatwords.bot.ChatWordsLongPollingBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
public class BotConfiguration {
    @Bean
    public DefaultBotOptions chatWordsBotOptions() {
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();
        defaultBotOptions.setMaxThreads(5);
        return defaultBotOptions;
    }

    @Bean
    @Profile("!local")
    public SetWebhook setWebhook(BotConfigurationProperties botConfigurationProperties) {
        return SetWebhook.builder()
                .url(computeCallbackEndpoint(botConfigurationProperties))
                .maxConnections(100)
                .build();
    }

    private String computeCallbackEndpoint(BotConfigurationProperties botConfigurationProperties) {
        final StringBuilder sb = new StringBuilder(botConfigurationProperties.getBotPath());
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append("callback/");
        sb.append(botConfigurationProperties.getToken());
        return sb.toString();
    }

    @Bean(name = "chatWordsBot")
    @Profile("!local")
    @Primary
    public ChatWordsBot chatWordsBot(
            DefaultBotOptions chatWordsBotOptions,
            BotConfigurationProperties botConfigurationProperties,
            SetWebhook setWebhook
    ) {
        return new ChatWordsBot(chatWordsBotOptions, botConfigurationProperties, setWebhook);
    }

    @Bean(name = "chatWordsBot")
    @Profile("local")
    @Primary
    public DefaultAbsSender chatWordsLongPollingBot(
            DefaultBotOptions chatWordsBotOptions,
            BotConfigurationProperties botConfigurationProperties
    ) {
        return new ChatWordsLongPollingBot(chatWordsBotOptions, botConfigurationProperties);
    }

    @Bean
    public ChatWordsInfoBot chatWordsInfoBot(BotConfigurationProperties botConfigurationProperties) {
        return new ChatWordsInfoBot(botConfigurationProperties);
    }
}
