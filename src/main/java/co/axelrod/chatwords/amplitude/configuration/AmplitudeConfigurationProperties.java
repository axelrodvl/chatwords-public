package co.axelrod.chatwords.amplitude.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties("amplitude")
public class AmplitudeConfigurationProperties {
    private String token;
}