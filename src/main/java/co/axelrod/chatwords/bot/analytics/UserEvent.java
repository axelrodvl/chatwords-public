package co.axelrod.chatwords.bot.analytics;

import co.axelrod.chatwords.bot.command.message.UserSettingsCommandProcessor;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.oxford.Level;
import co.axelrod.chatwords.exception.ChatWordsRuntimeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserEvent {
    USER_TRYING_TO_SWITCH_STORY_WHILE_BEING_IN_ANOTHER_STORY("User trying to switch story while being in another story"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    ALTERING_DICTIONARY("Altering dictionary"),
    TOO_LONG_DICTIONARY_NAME("Too long dictionary name"),
    TRYING_TO_USE_EXISTING_NAME_FOR_DICTIONARY("Trying to use existing name for dictionary"),
    INVALID_DICTIONARY_NAME("Invalid dictionary name"),
    RENAMED_DICTIONARY("Renamed dictionary"),
    REMOVED_DICTIONARY("Removed dictionary"),
    CHANGING_DICTIONARY("Changing dictionary"),
    CHANGED_DICTIONARY("Changed dictionary"),
    CHOSEN_INVALID_DICTIONARY("Chosen invalid dictionary"),
    CREATING_DICTIONARY("Creating dictionary"),
    ENTERED_DICTIONARY_TARGET_LANGUAGE("Entered dictionary target language"),
    NEW_DICTIONARY_CREATED("New dictionary created"),
    OPEN_FEEDBACK("Open feedback"),
    LEAVING_FEEDBACK("Leaving feedback"),
    NEW_FEEDBACK("New feedback"),
    MAIN_MENU("Main menu"),
    QUIZ_UNAVAILABLE("Quiz unavailable"),
    NEW_QUIZ("New quiz"),
    LEARNING_NEXT_WORD_FOR_QUIZ("Learning next word for quiz"),
    STARTING_QUIZ_AFTER_LEARNING("Starting quiz after learning"),
    FINISHED_QUIZ("Finished quiz"),
    REMOVED_WORD("Removed word"),
    CHANGED_DICTIONARY_DIRECTION("Changed dictionary direction"),
    CHANGED_DICTIONARY_SORTING("Changed dictionary sorting"),
    DETAILED_DICTIONARY_NEXT_PAGE("Detailed dictionary - Next page"),
    DETAILED_DICTIONARY_PREVIOUS_PAGE("Detailed dictionary - Previous page"),
    SHOWING_EMPTY_DICTIONARY("Showing empty dictionary"),
    SHOWING_DETAILED_DICTIONARY("Showing detailed dictionary"),
    SHOWING_DICTIONARY("Showing dictionary"),
    START("Start"),
    PASSED_INTRODUCTION("Passed introduction"),
    SETTING_1000_WORDS_DICTIONARY("Setting 1000 words dictionary"),
    CREATING_1000_WORDS_DICTIONARY("Creating 1000 words dictionary"),
    NEW_1000_WORDS_DICTIONARY_CREATED("New 1000 words dictionary created"),
    WORD_NOT_FOUND("Word not found"),
    WORD_TRANSLATED("Word translated"),
    DICTIONARY_OF_THE_DAY_REMINDER("Dictionary of the day reminder"),
    QUIZ_REMINDER("Quiz reminder"),
    CALL_WHEN_IN_MAINTENANCE_MODE("Call when in maintenance mode"),

    FROM_LANGUAGE_RUSSIAN("Target language: russian"),
    FROM_LANGUAGE_ENGLISH("Target language: english"),
    FROM_LANGUAGE_GERMAN("Target language: german"),
    FROM_LANGUAGE_SPANISH("Target language: spanish"),
    FROM_LANGUAGE_FRENCH("Target language: french"),
    FROM_LANGUAGE_ITALIAN("Target language: italian"),
    FROM_LANGUAGE_PORTUGUESE("Target language: portuguese"),
    FROM_LANGUAGE_TURKISH("Target language: turkish"),
    FROM_LANGUAGE_UKRAINIAN("Target language: ukrainian"),

    TARGET_LANGUAGE_RUSSIAN("Target language: russian"),
    TARGET_LANGUAGE_ENGLISH("Target language: english"),
    TARGET_LANGUAGE_GERMAN("Target language: german"),
    TARGET_LANGUAGE_SPANISH("Target language: spanish"),
    TARGET_LANGUAGE_FRENCH("Target language: french"),
    TARGET_LANGUAGE_ITALIAN("Target language: italian"),
    TARGET_LANGUAGE_PORTUGUESE("Target language: portuguese"),
    TARGET_LANGUAGE_TURKISH("Target language: turkish"),
    TARGET_LANGUAGE_UKRAINIAN("Target language: ukrainian"),

    USER_SETTINGS("User settings"),
    SETTING_QUIZ_LENGTH("Setting quiz length"),
    SETTING_USER_FROM_LANGUAGE("Setting user from language"),
    SETTING_INTERFACE_LANGUAGE("Setting interface language"),

    QUIZ_LENGTH_5("Quiz length: 5"),
    QUIZ_LENGTH_10("Quiz length: 10"),
    QUIZ_LENGTH_20("Quiz length: 20"),

    YOU_CAN_USE_TRANSLATE_SENTENCES_NOW("User notified about translation support"),

    INTERFACE_LANGUAGE_RUSSIAN("Interface language: russian"),
    INTERFACE_LANGUAGE_ENGLISH("Interface language: english"),
    INTERFACE_LANGUAGE_GERMAN("Interface language: german"),
    INTERFACE_LANGUAGE_SPANISH("Interface language: spanish"),
    INTERFACE_LANGUAGE_FRENCH("Interface language: french"),
    INTERFACE_LANGUAGE_ITALIAN("Interface language: italian"),
    INTERFACE_LANGUAGE_PORTUGUESE("Interface language: portuguese"),
    INTERFACE_LANGUAGE_TURKISH("Interface language: turkish"),
    INTERFACE_LANGUAGE_UKRAINIAN("Interface language: ukrainian"),

    TOO_LONG_SENTENCE_TRANSLATION_ATTEMPT("User tried to translate long sentence"),
    SENTENCE_TRANSLATION("Sentence translation"),
    YANDEX_TRANSLATE_TRANSLATION_FOR_NOT_FOUND_WORD("Calling translation for word not found in dictionary"),

    USER_HAS_DELETED_BOT("User deleted bot"),
    USER_HAS_RESTARTED_BOT_AFTER_DELETING("User restarted bot after deleting"),

    SHOWING_QUIZ_TUTORIAL("Showing quiz tutorial"),
    FINISHED_QUIZ_TUTORIAL("Finished quiz tutorial"),
    CHOOSING_QUIZ_TYPE("Choosing quiz type"),

    QUIZ_TYPE_NEW("Quiz type: new"),
    QUIZ_TYPE_REPEAT("Quiz type: repeat"),
    QUIZ_TYPE_RANDOM("Quiz type: random"),

    SHOW_LEARNED_WORDS_IN_DICTIONARY("Show learned words in dictionary"),
    HIDE_LEARNED_WORDS_IN_DICTIONARY("Hide learned words in dictionary"),

    DETAILED_WORD_TRANSLATION("Detailed word translation"),
    CHANGED_WORD_TRANSLATION_VARIANT("Change word translation variant"),

    USER_WANT_TO_TURN_ON_NOTIFICATIONS("User want to turn on notifications"),
    USER_TURNED_ON_NOTIFICATIONS("User turned on notifications"),
    USER_TURNED_OFF_NOTIFICATIONS("User turned off notifications"),
    USER_TURNED_OFF_ALL_NOTIFICATIONS("User turned off all notifications"),

    SHOW_AD("Show ad"),

    ADD_WORD_TO_DICTIONARY("Add word to dictionary"),
    ADD_WORD_FROM_SENTENCE_TO_DICTIONARY("Add word from sentence to dictionary"),
    USER_OPENED_ADD_TRANSLATION_BY_WORDS("User has opened add translation by words"),
    ADD_WORD_FROM_SENTENCE_TO_DICTIONARY_NEXT_PAGE("Add word from sentence to dictionary - Next page"),
    ADD_WORD_FROM_SENTENCE_TO_DICTIONARY_PREVIOUS_PAGE("Add word from sentence to dictionary - Previous page"),

    SETTING_OXFORD_3000_DICTIONARY("Setting Oxford 3000 dictionary"),
    CREATING_OXFORD_3000_DICTIONARY("Creating Oxford 3000 dictionary"),

    NEW_OXFORD_3000_DICTIONARY_CREATED("Oxford 3000 words dictionary created"),
    NEW_OXFORD_3000_A1_DICTIONARY_CREATED("Oxford 3000 (A1) words dictionary created"),
    NEW_OXFORD_3000_A2_DICTIONARY_CREATED("Oxford 3000 (A2) words dictionary created"),
    NEW_OXFORD_3000_B1_DICTIONARY_CREATED("Oxford 3000 (B1) words dictionary created"),
    NEW_OXFORD_3000_B2_DICTIONARY_CREATED("Oxford 3000 (B2) words dictionary created"),

    WORD_OF_THE_DAY_REMINDER("Word of the day reminder"),
    TURN_OFF_WORD_OF_THE_DAY_REMINDER("User turned off the word of the day reminder"),

    SENTENCE_TRANSLATION_UNAVAILABLE("Sentence translation unavailable"),

    ;

    private final String value;

    public static UserEvent fromValue(String value) {
        for (UserEvent text : UserEvent.values()) {
            if (text.value.equals(value)) {
                return text;
            }
        }
        return null;
    }

    public static UserEvent getNewOxford3000LevelDictionaryCreated(Level level) {
        switch (level) {

            case A1:
                return NEW_OXFORD_3000_A1_DICTIONARY_CREATED;
            case A2:
                return NEW_OXFORD_3000_A2_DICTIONARY_CREATED;
            case B1:
                return NEW_OXFORD_3000_B1_DICTIONARY_CREATED;
            case B2:
                return NEW_OXFORD_3000_B2_DICTIONARY_CREATED;
        }

        throw new ChatWordsRuntimeException("Invalid level");
    }

    public static UserEvent getFromLanguage(Language language) {
        switch (language) {
            case RUSSIAN:
                return FROM_LANGUAGE_RUSSIAN;
            case ENGLISH:
                return FROM_LANGUAGE_ENGLISH;
            case GERMAN:
                return FROM_LANGUAGE_GERMAN;
            case SPANISH:
                return FROM_LANGUAGE_SPANISH;
            case FRENCH:
                return FROM_LANGUAGE_FRENCH;
            case ITALIAN:
                return FROM_LANGUAGE_ITALIAN;
            case PORTUGUESE:
                return FROM_LANGUAGE_PORTUGUESE;
            case TURKISH:
                return FROM_LANGUAGE_TURKISH;
            case UKRAINIAN:
                return FROM_LANGUAGE_UKRAINIAN;
            default:
                throw new ChatWordsRuntimeException("Invalid language");
        }
    }

    public static UserEvent getTargetLanguage(Language language) {
        switch (language) {
            case RUSSIAN:
                return TARGET_LANGUAGE_RUSSIAN;
            case ENGLISH:
                return TARGET_LANGUAGE_ENGLISH;
            case GERMAN:
                return TARGET_LANGUAGE_GERMAN;
            case SPANISH:
                return TARGET_LANGUAGE_SPANISH;
            case FRENCH:
                return TARGET_LANGUAGE_FRENCH;
            case ITALIAN:
                return TARGET_LANGUAGE_ITALIAN;
            case PORTUGUESE:
                return TARGET_LANGUAGE_PORTUGUESE;
            case TURKISH:
                return TARGET_LANGUAGE_TURKISH;
            case UKRAINIAN:
                return TARGET_LANGUAGE_UKRAINIAN;
            default:
                throw new ChatWordsRuntimeException("Invalid language");
        }
    }

    public static UserEvent getInterfaceLanguage(String languageCode) {
        Language language = Language.getLanguageById(languageCode);
        switch (language) {
            case RUSSIAN:
                return INTERFACE_LANGUAGE_RUSSIAN;
            case ENGLISH:
                return INTERFACE_LANGUAGE_ENGLISH;
            case GERMAN:
                return INTERFACE_LANGUAGE_GERMAN;
            case SPANISH:
                return INTERFACE_LANGUAGE_SPANISH;
            case FRENCH:
                return INTERFACE_LANGUAGE_FRENCH;
            case ITALIAN:
                return INTERFACE_LANGUAGE_ITALIAN;
            case PORTUGUESE:
                return INTERFACE_LANGUAGE_PORTUGUESE;
            case TURKISH:
                return INTERFACE_LANGUAGE_TURKISH;
            case UKRAINIAN:
                return INTERFACE_LANGUAGE_UKRAINIAN;
            default:
                throw new ChatWordsRuntimeException("Invalid language");
        }
    }

    public static UserEvent getQuizLength(Integer length) {
        if (length.equals(UserSettingsCommandProcessor.QUIZ_LENGTH_SHORT)) {
            return QUIZ_LENGTH_5;
        }

        if (length.equals(UserSettingsCommandProcessor.QUIZ_LENGTH_MEDIUM)) {
            return QUIZ_LENGTH_10;
        }

        if (length.equals(UserSettingsCommandProcessor.QUIZ_LENGTH_LONG)) {
            return QUIZ_LENGTH_20;
        }

        throw new ChatWordsRuntimeException("Invalid quiz length");
    }
}