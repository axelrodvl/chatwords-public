package co.axelrod.chatwords.dictionary.provider.yandex;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.Dictionary;
import co.axelrod.chatwords.dictionary.provider.yandex.configuration.YandexDictionaryConfigurationProperties;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Definition;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.YandexDictionaryResponse;
import co.axelrod.chatwords.storage.Word;
import co.axelrod.chatwords.storage.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionaryParser.requestWord;
import static co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionaryParser.shortTranslation;

@Component
@RequiredArgsConstructor
@Slf4j
public class YandexDictionary implements Dictionary {
    private static final int SHORT_POS = 2;
    private static final int MORPHO = 4;

    private final RestTemplate restTemplate = new RestTemplate();

    private final YandexDictionaryConfigurationProperties properties;
    private final WordRepository wordRepository;

    public Optional<YandexDictionaryResponse> callYandexDictionary(String word, String uiLanguage, Language source, Language target) {
        log.info("Call for Yandex for word " + word + " from " + source.getValue() + " to " + target.getValue());

        String url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup"
                + "?key=" + properties.getToken()
                + "&lang=" + source.getValue() + "-" + target.getValue()
                + "&flags=" + MORPHO
                + "&ui=" + uiLanguage
                + "&text=" + word;

        YandexDictionaryResponse response = restTemplate.getForObject(url, YandexDictionaryResponse.class);

        if (response == null
                || response.getDef() == null
                || response.getDef().isEmpty()
                || response.getDef().get(0).getTr().isEmpty()) {
            return Optional.empty();
        }

        // Сортируем переводы внутри каждого определения по частоте использования
        response.getDef().forEach(definition -> definition.getTr().sort((translation1, translation2) -> {
            Integer firstFrequency = (Integer) translation1.getAdditionalProperties().get("fr");
            Integer secondFrequency = (Integer) translation2.getAdditionalProperties().get("fr");

            if (firstFrequency != null && secondFrequency != null) {
                return secondFrequency.compareTo(firstFrequency);
            } else {
                return 0;
            }
        }));

        // Сортируем перевод по частоте использования
        response.getDef().sort((firstDefinition, secondDefinition) -> {
            Integer firstMaxFrequency = (Integer) firstDefinition.getTr().get(0).getAdditionalProperties().get("fr");
            Integer secondMaxFrequency = (Integer) secondDefinition.getTr().get(0).getAdditionalProperties().get("fr");

            if (firstMaxFrequency != null && secondMaxFrequency != null) {
                return secondMaxFrequency.compareTo(firstMaxFrequency);
            } else {
                return 0;
            }
        });

        return Optional.of(response);
    }

    @Override
    public Optional<Word> translate(String request, String languageCode, Language source, Language target) {
        // Поиск существующего слова в БД
        Optional<Word> word = wordRepository.findWord(request, source);

        // Слово есть в БД и есть перевод на искомый язык
        if (word.isPresent() && word.get().getTranslation(target) != null && word.get().getFullTranslationWithEmoji(source, target) != null) {
            return word;
        }

        // Слово найдено, но нет перевода на искомый язык (новая языковая пара)
        if (word.isPresent()) {
            // Добавляем перевод на искомый язык
            Optional<YandexDictionaryResponse> translation = callYandexDictionary(request, languageCode, source, target);
            if (translation.isPresent()) {
                // Нашли перевод на искомый язык, обновляем слово и возвращаем ответ
                Definition definition = getDefinitionByRequest(request, translation.get());
                word.get().setTranslation(shortTranslation(definition), target);
                word.get().setFullTranslation(source, target, translation.get());

                // Ищем обратный перевод
                Optional<YandexDictionaryResponse> backwardTranslation = callYandexDictionary(word.get().getTranslation(target), languageCode, target, source);
                if (backwardTranslation.isPresent()) {
                    // Нашли обратный перевод - дополняем слово, сохраняем и возвращаем ответ
                    word.get().setFullTranslation(target, source, backwardTranslation.get());
                    wordRepository.save(word.get());
                    return word;
                } else {
                    // Не нашли обратный перевод
                    return Optional.empty();
                }
            } else {
                // Не нашли перевод слова на искомый язык
                return Optional.empty();
            }
        }

        // Слова не найдено - пробуем найти перевод в обратную сторону
        word = wordRepository.findWord(request, target);

        // Слово есть в БД и есть перевод на искомый язык
        if (word.isPresent() && word.get().getTranslation(source) != null && word.get().getFullTranslationWithEmoji(target, source) != null) {
            return word;
        }

        // Слово не найдено - переводим
        Optional<YandexDictionaryResponse> translation = callYandexDictionary(request, languageCode, source, target);
        if (translation.isPresent()) {
            // Нашли перевод - создаем слово (только если есть перевод в обе стороны)
            Word newWord = new Word();

            Definition definition = getDefinitionByRequest(request, translation.get());

            newWord.setTranslation(requestWord(definition), source);
            newWord.setTranslation(shortTranslation(definition), target);
            newWord.setFullTranslation(source, target, translation.get());

            // Ищем обратный перевод
            Optional<YandexDictionaryResponse> backwardTranslation = callYandexDictionary(newWord.getTranslation(target), languageCode, target, source);
            if (backwardTranslation.isPresent()) {
                // Нашли обратный перевод - дополняем слово, сохраняем и возвращаем ответ
                newWord.setFullTranslation(target, source, backwardTranslation.get());
                wordRepository.save(newWord);
                return Optional.of(newWord);
            } else {
                // Не нашли обратный перевод
                return Optional.empty();
            }
        } else {
            // Не нашли перевода на искомый язык
            return Optional.empty();
        }
    }

    private Definition getDefinitionByRequest(String request, YandexDictionaryResponse translation) {
        return translation.getDef()
                .stream()
                .filter(definition -> definition.getText().equalsIgnoreCase(request))
                .findFirst()
                .orElse(translation.getDef().get(0));
    }
}
