package co.axelrod.chatwords.validator;

import co.axelrod.chatwords.validator.model.ValidationResult;

public abstract class Validator {
    abstract ValidationResult validate(String input);
}
