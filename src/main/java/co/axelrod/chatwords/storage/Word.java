package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.provider.yandex.YandexDictionaryParser;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.Definition;
import co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary.YandexDictionaryResponse;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Document
@Getter
@Setter
public class Word {
    public Word() {

    }

    public Word(Word word) {
        this.russian = word.russian;
        this.english = word.english;
        this.german = word.german;
        this.spanish = word.spanish;
        this.french = word.french;
        this.italian = word.italian;
        this.portuguese = word.portuguese;
        this.turkish = word.turkish;
        this.ukrainian = word.ukrainian;
        this.translations = word.translations;
    }

    @Id
    private String id;

    @Indexed(sparse = true)
    private String russian;

    @Indexed(sparse = true)
    private String english;

    @Indexed(sparse = true)
    private String german;

    @Indexed(sparse = true)
    private String spanish;

    @Indexed(sparse = true)
    private String french;

    @Indexed(sparse = true)
    private String italian;

    @Indexed(sparse = true)
    private String portuguese;

    @Indexed(sparse = true)
    private String turkish;

    @Indexed(sparse = true)
    private String ukrainian;

    private Map<String, YandexDictionaryResponse> translations = new HashMap<>();

    public void setTranslation(String value, Language language) {
        switch (language) {
            case RUSSIAN:
                this.russian = value;
                break;
            case ENGLISH:
                this.english = value;
                break;
            case GERMAN:
                this.german = value;
                break;
            case SPANISH:
                this.spanish = value;
                break;
            case FRENCH:
                this.french = value;
                break;
            case ITALIAN:
                this.italian = value;
                break;
            case PORTUGUESE:
                this.portuguese = value;
                break;
            case TURKISH:
                this.turkish = value;
                break;
            case UKRAINIAN:
                this.ukrainian = value;
                break;
            default:
                throw new ChatWordsRuntimeException("Invalid language for translation");
        }
    }

    public void setFullTranslation(Language sourceLanguage, Language targetLanguage, YandexDictionaryResponse translation) {
        this.translations.put(
                sourceLanguage.getValue() + "_" + targetLanguage.getValue(),
                translation
        );
    }

    public Language getLanguage(String value) {
        if (value.equalsIgnoreCase(russian)) return Language.RUSSIAN;
        if (value.equalsIgnoreCase(english)) return Language.ENGLISH;
        if (value.equalsIgnoreCase(german)) return Language.GERMAN;
        if (value.equalsIgnoreCase(spanish)) return Language.SPANISH;
        if (value.equalsIgnoreCase(french)) return Language.FRENCH;
        if (value.equalsIgnoreCase(italian)) return Language.ITALIAN;
        if (value.equalsIgnoreCase(portuguese)) return Language.PORTUGUESE;
        if (value.equalsIgnoreCase(turkish)) return Language.TURKISH;
        if (value.equalsIgnoreCase(ukrainian)) return Language.UKRAINIAN;
        return null;
    }

    public String getTranslation(Language language) {
        switch (language) {
            case RUSSIAN:
                return russian;
            case ENGLISH:
                return english;
            case GERMAN:
                return german;
            case SPANISH:
                return spanish;
            case FRENCH:
                return french;
            case ITALIAN:
                return italian;
            case PORTUGUESE:
                return portuguese;
            case TURKISH:
                return turkish;
            case UKRAINIAN:
                return ukrainian;
            default:
                throw new ChatWordsRuntimeException("Invalid language");
        }
    }

    public String getTranslationWithEmoji(Language language) {
        return language.getEmoji() + " " + getTranslation(language);
    }

    public String getFormattedTranslation(Language source, Language target) {
        String fullTranslation = getFullTranslation(source, target);
        if (fullTranslation == null) {
            return null;
        }

        String transcription = getTranscription(source, target);

        if (transcription == null) {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b>\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b>"
                    + "\n\n"
                    + fullTranslation;
        }

        if (source.equals(Language.RUSSIAN) || source.equals(Language.UKRAINIAN)) {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b>\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b> [" + transcription + "]"
                    + "\n\n"
                    + fullTranslation;
        } else {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b> [" + transcription + "]\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b>"
                    + "\n\n"
                    + fullTranslation;
        }
    }

    public String getDetailedFormattedTranslation(Language source, Language target) {
        String fullTranslation = getDetailedFullTranslation(source, target);
        if (fullTranslation == null) {
            return null;
        }

        String transcription = getTranscription(source, target);

        if (transcription == null) {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b>\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b>"
                    + "\n\n"
                    + fullTranslation;
        }

        if (source.equals(Language.RUSSIAN) || source.equals(Language.UKRAINIAN)) {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b>\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b> [" + transcription + "]"
                    + "\n\n"
                    + fullTranslation;
        } else {
            return source.getEmoji() + " <b>" + getTranslation(source) + "</b> [" + transcription + "]\n"
                    + target.getEmoji() + " <b>" + getTranslation(target) + "</b>"
                    + "\n\n"
                    + fullTranslation;
        }
    }

    public String getFullTranslationWithEmoji(Language source, Language target) {
        String fullTranslation = getFullTranslation(source, target);
        if (fullTranslation == null) {
            return null;
        }
        return source.getEmoji() + target.getEmoji() + "\n" + fullTranslation;
    }

    public String getFullTranslation(Language source, Language target) {
        String key = source.getValue() + "_" + target.getValue();
        if (translations.get(key) == null) {
            return null;
        } else {
            return YandexDictionaryParser.fullTranslation(
                    getTranslation(source),
                    getTranslation(target),
                    translations.get(key)
            );
        }
    }

    public String getDetailedFullTranslation(Language source, Language target) {
        String key = source.getValue() + "_" + target.getValue();
        if (translations.get(key) == null) {
            return null;
        } else {
            return YandexDictionaryParser.fullDetailedTranslation(
                    getTranslation(source),
                    getTranslation(target),
                    translations.get(key)
            );
        }
    }

    public YandexDictionaryResponse getRawTranslation(Language source, Language target) {
        String key = source.getValue() + "_" + target.getValue();
        if (translations.get(key) == null) {
            return null;
        } else {
            return translations.get(key);
        }
    }

    public String getTranscription(Language source, Language target) {
        Language sourceTranscriptionLanguage = (source.equals(Language.RUSSIAN) || source.equals(Language.UKRAINIAN)) ? target : source;
        Language targetTranscriptionLanguage = (source.equals(Language.RUSSIAN) || source.equals(Language.UKRAINIAN)) ? source : target;

        String key = sourceTranscriptionLanguage.getValue() + "_" + targetTranscriptionLanguage.getValue();
        if (translations.get(key) == null) {
            return null;
        } else {
            YandexDictionaryResponse yandexDictionaryResponse = translations.get(key);
            Definition definition = yandexDictionaryResponse.getDef().stream()
                    .filter(def -> def.getText().equals(getTranslation(sourceTranscriptionLanguage)))
                    .findAny().orElse(null);

            if (definition == null) {
                return null;
            }

            return (String) definition.getAdditionalProperties().get("ts");
        }
    }
}
