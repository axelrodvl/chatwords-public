package co.axelrod.chatwords.amplitude.configuration;

import com.amplitude.Amplitude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmplitudeConfiguration {
    @Bean
    public Amplitude client(AmplitudeConfigurationProperties configurationProperties) {
        Amplitude client = Amplitude.getInstance();
        client.init(configurationProperties.getToken());
        return client;
    }
}
