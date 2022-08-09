package co.axelrod.chatwords.dictionary.provider.yandex;

import co.axelrod.chatwords.dictionary.provider.yandex.configuration.YandexDictionaryConfigurationProperties;
import co.axelrod.chatwords.storage.SpeechKitFile;
import co.axelrod.chatwords.storage.SpeechKitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class YandexSpeechKit {
    private final RestTemplate restTemplate = new RestTemplate();

    private final YandexDictionaryConfigurationProperties properties;
    private final SpeechKitRepository speechKitRepository;

    public InputFile getSpeech(String input) {
        byte[] response;

        SpeechKitFile speechKitFile = speechKitRepository.findById(input).orElse(null);
        if (speechKitFile != null) {
            log.info("Using saved Yandex.SpeechKit file for input " + input);
            response = speechKitFile.getFile().getData();
        } else {
            response = null;
//            response = getOggFromYandexSpeechKit(input);
        }

        if (response != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response);
            return new InputFile(byteArrayInputStream, "chatWords_" + input + ".ogg");
        } else {
            return null;
        }
    }

    @Nullable
    private byte[] getOggFromYandexSpeechKit(String input) {
        byte[] response;
        SpeechKitFile speechKitFile;
        log.info("Call for Yandex.SpeechKit for input " + input);

            /*
            curl -X POST \
              -H "Authorization: Bearer ${IAM_TOKEN}" \
              --data-urlencode "text=astonishing" \
              -d "lang=en-US&folderId=b1g1voqf1u5td0o4lhf1" \
              "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize" >speech.ogg
             */

        String url = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Api-Key " + properties.getApiKey());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("lang", "en-US");
        map.add("folderId", "b1g1voqf1u5td0o4lhf1");
        map.add("voice", "nick");
        map.add("text", input);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        response = restTemplate.postForObject(url, request, byte[].class);

        if (response != null) {
            speechKitFile = new SpeechKitFile();
            speechKitFile.setId(input);
            speechKitFile.setFile(new Binary(BsonBinarySubType.BINARY, response));
            speechKitRepository.save(speechKitFile);
        } else {
            log.error("Empty response from Yandex.SpeechKit");
        }
        return response;
    }
}
