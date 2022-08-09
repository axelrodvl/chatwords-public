package co.axelrod.chatwords.bot.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties("quiz")
public class QuizConfigurationProperties {
    private Integer wordsCount;
    private Integer variants;
}
