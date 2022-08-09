package co.axelrod.chatwords.validator;

import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.validator.model.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class DictionaryNameValidator extends Validator {
    @Override
    public ValidationResult validate(String dictionaryName) {
        if (dictionaryName.length() > 32) {
            return new ValidationResult(false, Text.TOO_LONG_DICTIONARY_NAME, UserEvent.TOO_LONG_DICTIONARY_NAME);
        }

        // Проверка на точки и знак доллара
        if (dictionaryName.contains(".") || dictionaryName.contains("$")) {
            return new ValidationResult(false, Text.INVALID_DICTIONARY_NAME, UserEvent.TRYING_TO_USE_EXISTING_NAME_FOR_DICTIONARY);
        }

        return new ValidationResult(true);
    }
}
