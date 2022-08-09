package co.axelrod.chatwords.dictionary.provider.yandex;

import co.axelrod.chatwords.dictionary.model.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class YandexSpellCheck {
    private static final int IGNORE_DIGITS = 2;
    private static final int IGNORE_URLS = 4;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String correctSpelling(String input, Language source, Language target) {
        // Skipping for all
        return input;
//
//        boolean supportedSourceLanguage = source.equals(Language.RUSSIAN) || source.equals(Language.ENGLISH) || source.equals(Language.UKRAINIAN);
//        boolean supportedTargetLanguage = target.equals(Language.RUSSIAN) || target.equals(Language.ENGLISH) || target.equals(Language.UKRAINIAN);
//
//        if (!(supportedSourceLanguage && supportedTargetLanguage)) {
//            log.info("Skipping spelling for unsupported language");
//            return input;
//        }
//
//        log.info("Correcting spelling with Yandex.Speller for input: " + input);
//
//        String url = "https://speller.yandex.net/services/spellservice.json/checkText"
//                + "?options=" + (IGNORE_DIGITS + IGNORE_URLS)
//                + "&lang=" + source.getValue() + "," + target.getValue()
//                + "&text=" + String.join("+", input.split(" "));
//
//        try {
//            String response = restTemplate.getForObject(url, String.class);
//            SpelledWord[] spelledWords = objectMapper.readValue(response, SpelledWord[].class);
//
//            String fixedInput = input;
//
//            for (SpelledWord spelledWord : spelledWords) {
//                String originalWord = spelledWord.getWord();
//                String fixedWord = spelledWord.getS().get(0);
//                if (fixedInput.contains(originalWord)) {
//                    fixedInput = fixedInput.replace(originalWord, fixedWord);
//                }
//            }
//
//            return fixedInput;
//        } catch (Exception ex) {
//            log.error("Unable to get spell check", ex);
//            return input;
//        }
    }
}
