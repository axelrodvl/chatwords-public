package co.axelrod.chatwords.bot;

import co.axelrod.chatwords.bot.command.RequestProcessor;
import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class ChatWordsLongPollingBot extends TelegramLongPollingBot {
    private final BotConfigurationProperties botConfigurationProperties;

    @Autowired
    @Lazy
    private RequestProcessor requestProcessor;

    public ChatWordsLongPollingBot(DefaultBotOptions chatWordsBotOptions, BotConfigurationProperties botConfigurationProperties) {
        super(chatWordsBotOptions);
        this.botConfigurationProperties = botConfigurationProperties;
    }

    @Override
    public String getBotUsername() {
        return botConfigurationProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botConfigurationProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        requestProcessor.processUpdate(update);
    }
}
