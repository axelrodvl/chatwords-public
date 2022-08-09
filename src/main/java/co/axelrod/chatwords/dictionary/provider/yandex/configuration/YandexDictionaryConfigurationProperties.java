package co.axelrod.chatwords.dictionary.provider.yandex.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties("yandex")
public class YandexDictionaryConfigurationProperties {
    private String token;
    private String apiKey;
}
