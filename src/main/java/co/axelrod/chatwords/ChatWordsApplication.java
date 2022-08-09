package co.axelrod.chatwords;

import co.axelrod.chatwords.amplitude.configuration.AmplitudeConfigurationProperties;
import co.axelrod.chatwords.bot.configuration.BotConfigurationProperties;
import co.axelrod.chatwords.bot.configuration.QuizConfigurationProperties;
import co.axelrod.chatwords.dictionary.provider.yandex.configuration.YandexDictionaryConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties({
        BotConfigurationProperties.class,
        QuizConfigurationProperties.class,
        AmplitudeConfigurationProperties.class,
        YandexDictionaryConfigurationProperties.class
})
@EnableAsync
@EnableCaching
public class ChatWordsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatWordsApplication.class, args);
    }
}
