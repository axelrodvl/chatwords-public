package co.axelrod.chatwords.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Callback {
    REMOVE_WORD("remove_word", "rw"),
    CHANGE_WORD_TRANSLATION_VARIANT("changeWordTranslationVariant", "cwtv"),

    SHOW_DICTIONARY("show_dictionary", "sd"),
    DETAILED_DICTIONARY("detailed_dictionary", "dd"),
    ALTER_DICTIONARY("alter_dictionary", "ad"),
    CHANGE_DICTIONARY("change_dictionary", "cd"),

    QUIZ("quiz", "qz"),
    SKIP_WORDS_LEARNING("skipWordsLearning", "swl"),

    CHANGE_DIRECTION("changeDirection", "cdd"),
    CHANGE_SORTING("changeSorting", "changeSorting"),
    CHANGE_DIRECTION_DETAILED("changeDirectionDetailed", "cddd"),

    UNKNOWN("UNKNOWN", "unk"),

    NEXT("next", "next"),
    FINISH_QUIZ_TUTORIAL("finishQuizTutorial", "finishQuizTutorial"),
    CANCEL("cancel", "cancel"),

    NEXT_PAGE("nextPage", "np"),
    PREVIOUS_PAGE("previousPage", "pp"),

    ADD_DICTIONARY("addDictionary", "add"),

    SET_QUIZ_LENGTH("setQuizLength", "sql"),

    TARGET_LANGUAGE("targetLanguage", "tl"),

    LANGUAGE_CODE("languageCode", "languageCode"),

    QUIZ_TYPE("quizType", "quizType"),

    QUIZ_WORD_ACTION("quizWordAction", "quizWordAction"),
    QUIZ_REMOVE_WORD("quizRemoveWord", "qrw"),

    MY_LANGUAGE("myLanguage", "myLanguage"),
    CHANGE_TRANSLATION_LANGUAGE("changeTranslationLanguage", "ctl"),
    SKIP("skip", "skip"),

    HIDE_LEARNED("showLearned", "showLearned"),
    SHOW_LEARNED("hideLearned", "hideLearned"),

    CONTINUE_QUIZ("continueQuiz", "continueQuiz"),

    ADD_WORD_TO_DICTIONARY("addWordToDictionary", "addword"),
    ADD_SENTENCE_WORD_TO_DICTIONARY("addSentenceWordToDictionary", "addwordsnt"),

    CHANGE_TRANSLATION_MODE("changeTranslationMode", "changeTranslationMode"),

    TURN_ON_NOTIFICATIONS("turnOnNotifications", "turnOnNotifications"),
    TURN_OFF_NOTIFICATIONS("turnOffNotifications", "turnOffNotifications"),
    TURN_OFF_ALL_NOTIFICATIONS("turnOffAllNotifications", "turnOffAllNotifications"),
    SET_NOTIFICATION_TIME("setNotificationTime", "setNotificationTime"),
    SET_NOTIFICATION_TIME_ZONE("setNotificationTimeZone", "setNotificationTimeZone"),

    SET_NOTIFICATION_TIME_ZONE_MANUALLY("setNotificationTimeZoneManually", "setNotificationTimeZoneManually"),

    SENTENCE_TO_DICTIONARY("sentenceToDictionary", "sentenceToDictionary"),

    OXFORD_3000_LEVEL("oxford3000Level", "oxford3000Level"),

    DETAILED_WORD_TRANSLATION("detailedWordTranslation", "detailedWordTranslation"),

    TURN_OFF_WORD_OF_THE_DAY_NOTIFICATIONS("turnOffWordOfTheDay", "turnOffWordOfTheDay"),
    ;

    private final String value;
    private final String id;

    public static Callback getCallback(String name) {
        for (Callback command : values()) {
            if (command.value.equals(name)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
