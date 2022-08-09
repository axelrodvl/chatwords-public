package co.axelrod.chatwords.dictionary.provider.yandex;

import co.axelrod.chatwords.cache.ChatWordsCache;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.yandex.configuration.YandexDictionaryConfigurationProperties;
import co.axelrod.chatwords.dictionary.provider.yandex.model.translation.PossibleLanguage;
import co.axelrod.chatwords.dictionary.provider.yandex.model.translation.Translation;
import co.axelrod.chatwords.dictionary.provider.yandex.model.translation.YandexTranslateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class
YandexTranslate {
    private final RestTemplate restTemplate = new RestTemplate();

    private final YandexDictionaryConfigurationProperties properties;

    // TODO Turned off
//    @Cacheable(value = ChatWordsCache.TRANSLATIONS, unless = "#result == null")
    private Translation translate(String input, Language source, Language target) {
        if (source == null) {
            source = Language.ENGLISH;
        }

        PossibleLanguage possibleLanguage = getPossibleLanguage(input, source, target);

        log.info("Call for Yandex.Translate for phrase " + input + " from " + source.getValue() + " to " + target.getValue());

        String url = "https://translate.api.cloud.yandex.net/translate/v2/translate";

        JSONObject requestBody = new JSONObject();
        requestBody.put("folderId", "b1g1voqf1u5td0o4lhf1");
        requestBody.put("texts", Collections.singleton(input));
        requestBody.put("sourceLanguageCode", possibleLanguage.getLanguageCode());
        requestBody.put("targetLanguageCode", possibleLanguage.getLanguageCode().equals(source.getValue()) ? target.getValue() : source.getValue());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Api-Key " + properties.getApiKey());

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        YandexTranslateResponse response = restTemplate.postForObject(url, request, YandexTranslateResponse.class);

        if (response.getTranslations().get(0).getDetectedLanguageCode() == null) {
            response.getTranslations().get(0).setDetectedLanguageCode(possibleLanguage.getLanguageCode());
        }

        if (response != null) {
            log.info("Translated: " + response.toString());
            return response.getTranslations().get(0);
        } else {
            Translation translation = new Translation();
            translation.setDetectedLanguageCode(target.getValue());
            translation.setText("Перевод не найден.");
            return translation;
        }
    }

    // TODO Turned off
//    @Cacheable(value = ChatWordsCache.DETERMINE_LANGUAGE, unless = "#result == null")
    private Language determineLanguage(String input, Language source, Language target) {
        return Language.getLanguageById(getPossibleLanguage(input, source, target).getLanguageCode());
    }

    // TODO Turned off
//    @Cacheable(value = ChatWordsCache.POSSIBLE_LANGUAGE, unless = "#result == null")
    private PossibleLanguage getPossibleLanguage(String input, Language source, Language target) {
        if (source == null) {
            source = Language.ENGLISH;
        }

        if (input.length() > 100) {
            input = input.substring(0, 100);
        }

        log.info("Determining possible language for " + input + " from " + source.getValue() + " to " + target.getValue());

        String url = "https://translate.api.cloud.yandex.net/translate/v2/detect";

        JSONObject requestBody = new JSONObject();
        requestBody.put("folderId", "b1g1voqf1u5td0o4lhf1");
        requestBody.put("text", input);
        requestBody.put("languageCodeHints", Arrays.asList(source.getValue(), target.getValue()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Api-Key " + properties.getApiKey());

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        PossibleLanguage response = restTemplate.postForObject(url, request, PossibleLanguage.class);

        if (response == null) {
            PossibleLanguage possibleLanguage = new PossibleLanguage();
            possibleLanguage.setLanguageCode(source.getValue());
            return possibleLanguage;
        }

        return response;
    }
}
