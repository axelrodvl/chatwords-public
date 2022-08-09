package co.axelrod.chatwords.bot.webhook;

import co.axelrod.chatwords.bot.ChatWordsBot;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Profile("!local")
@RestController
public class WebhookController {
    private final ChatWordsBot chatWordsBot;

    public WebhookController(ChatWordsBot chatWordsBot) {
        this.chatWordsBot = chatWordsBot;
    }

    @PostMapping("/callback/${bot.token}")
    @ResponseBody
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return chatWordsBot.onWebhookUpdateReceived(update);
    }
}
