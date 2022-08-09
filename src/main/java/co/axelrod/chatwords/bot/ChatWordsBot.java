package co.axelrod.chatwords.bot;

import co.axelrod.chatwords.bot.command.RequestProcessor;
import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.ApiResponse;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Slf4j
public class ChatWordsBot extends SpringWebhookBot {
    private final BotConfigurationProperties botConfigurationProperties;
    private final SetWebhook setWebhook;

    @Autowired
    @Lazy
    private RequestProcessor requestProcessor;

    public ChatWordsBot(
            DefaultBotOptions chatWordsBotOptions,
            BotConfigurationProperties botConfigurationProperties,
            SetWebhook setWebhook
    ) {
        super(chatWordsBotOptions, setWebhook);
        this.botConfigurationProperties = botConfigurationProperties;
        this.setWebhook = setWebhook;
        init();
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
    public String getBotPath() {
        return botConfigurationProperties.getBotPath();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        requestProcessor.processUpdate(update);
        return null;
    }

    @SneakyThrows
    private void init() {
        setWebhook(setWebhook);
    }

    @Override
    public void setWebhook(SetWebhook setWebhook) throws TelegramApiException {
        try {
            final RestTemplate rest = new RestTemplate();
            final HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Accept", "application/json");

            final String setWebhookUrl = String.format("https://api.telegram.org/bot%s/%s", getBotToken(), SetWebhook.PATH);
            rest.exchange(setWebhookUrl, HttpMethod.POST, new HttpEntity<>(setWebhook, headers), ApiResponse.class);
        } catch (Exception e) {
            throw new TelegramApiRequestException("Error executing setWebHook method", e);
        }
    }
}
