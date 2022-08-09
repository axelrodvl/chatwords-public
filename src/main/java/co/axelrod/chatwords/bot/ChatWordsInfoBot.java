package co.axelrod.chatwords.bot;

import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Slf4j
public class ChatWordsInfoBot extends TelegramLongPollingBot {
    private final BotConfigurationProperties botConfigurationProperties;

    @Override
    public String getBotUsername() {
        return botConfigurationProperties.getInfoName();
    }

    @Override
    public String getBotToken() {
        return botConfigurationProperties.getInfoToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Used only to send messages
    }
}
