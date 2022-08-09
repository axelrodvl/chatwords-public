package co.axelrod.chatwords.bot.i18n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Text {
    ERROR("error"),
    TRANSLATION_NOT_FOUND("translationNotFound"),

    RUSSIAN("russian"),
    ENGLISH("english"),
    GERMAN("german"),
    SPANISH("spanish"),
    FRENCH("french"),
    ITALIAN("italian"),
    PORTUGUESE("portuguese"),
    TURKISH("turkish"),
    UKRAINIAN("ukrainian"),

    CHOOSE_ONE_OF_SUPPORTED_LANGUAGES("chooseOneOfSupportedLanguages"),
    CHOOSE_LEARNING_LANGUAGE("chooseLearningLanguage"),

    DELETE("delete"),
    DICTIONARY_IS_EMPTY("dictionaryIsEmpty"),

    IN_DETAIL("inDetail"),
    WITHOUT_DETAILS("withoutDetails"),

    ADD_WORDS_TO_REPEAT("addWordsToRepeat"),
    GOOD_JOB("goodJob"),
    TIME_FOR_QUIZ("timeForQuiz"),
    QUIZ("quiz"),

    CHOOSE_DICTIONARY("chooseDictionary"),
    CHOOSE_TARGET_LANGUAGE("chooseTargetLanguage"),
    SET_DICTIONARY_NAME("setDictionaryName"),
    SET_DICTIONARY_NAME_OR_CHOOSE_SUGGESTED("setDictionaryNameOrChooseSuggested"),
    CHOOSE_EXISTING_DICTIONARY("chooseExistingDictionary"),

    CHOOSE_ACTION("chooseAction"),

    REPEAT_QUIZ("repeatQuiz"),

    START_QUIZ("startQuiz"),

    LEARN_1000_WORDS("learn1000Words"),
    LEARN_OXFORD_3000_WORDS("learnOxford3000Words"),

    CANCEL("cancel"),

    KNOWN_WORDS("knownWords"),
    DICTIONARY_LEARNT("dictionaryLearnt"),
    DICTIONARY_LEARNT_FOR_PERCENT("dictionaryLearntForPercent"),

    SHOW_DICTIONARY("showDictionary"),

    ACTION_UNAVAILABLE("actionUnavailable"),

    DICTIONARY_WITH_THIS_NAME_ALREADY_EXISTS("dictionaryWithThisNameAlreadyExists"),
    INVALID_DICTIONARY_NAME("invalidDictionaryName"),
    TOO_LONG_DICTIONARY_NAME("tooLongDictionaryName"),
    INVALID_DICTIONARY_TARGET_LANGUAGE("invalidDictionaryTargetLanguage"),

    NEXT("next"),

    LEARN_FOLLOWING_QUIZ_WORDS("learnFollowingQuizWords"),

    RENAME_DICTIONARY("renameDictionary"),
    DELETE_DICTIONARY("deleteDictionary"),
    YES_DELETE_DICTIONARY("yesDeleteDictionary"),

    SKIP_WORDS_LEARNING("skipWordsLearning"),
    ALTER_DICTIONARY("alter_dictionary"),

    MY_LANGUAGES("myLanguages"),
    MY_WORDS("myWords"),
    CURRENT_DICTIONARY("currentDictionary"),

    LEAVE_REVIEW("leaveReview"),

    CHAT_WORDS("chatWords"),
    VERSION("version"),
    ABOUT("about"),
    CONTACT_ME("contactMe"),
    EXIT("exit"),
    ENTER_FEEDBACK("enterFeedback"),
    THANK_YOU_FOR_FEEDBACK("thankYouForFeedback"),

    CHANGE_DIRECTION("changeDirection"),

    YANDEX_TERM("yandexTerm"),

    NEXT_PAGE("nextPage"),
    PREVIOUS_PAGE("previousPage"),

    ADD_THIS_DICTIONARY("addThisDictionary"),
    DICITONARY_OF_THE_DAY("dictionaryOfTheDay"),

    USER_SETTINGS("userSettings"),
    QUIZ_LENGTH("quizLength"),
    CHOOSE_QUIZ_LENGTH("chooseQuizLength"),

    MAINTENANCE_MODE("maintenanceMode"),

    THOUSAND_DICTIONARY_NAME("thousandDictionaryName"),

    BEGINNING_QUIZ("beginningQuiz"),
    FIRST_QUIZ_WORD("firstQuizWord"),
    NEXT_QUIZ_WORD("nextQuizWord"),
    QUIZ_CORRECT_ANSWER("quizCorrectAnswer"),
    QUIZ_COMPLETED("quizCompleted"),
    QUIZ_CORRECT_ANSWERS_COUNT("quizCorrectAnswersCount"),
    QUIZ_ANSWER_IS_CORRECT("quizAnswerIsCorrect"),
    QUIZ_ANSWER_IS_INCORRECT("quizAnswerIsIncorrect"),
    QUIZ_WORDS_LEFT("quizWordsLeft"),

    DICTIONARY("dictionary"),

    YOU_ARE_GOING_TO_DELETE_DICTIONARY("youAreGoingToDeleteDictionary"),
    ARE_YOU_SURE("areYouSure"),
    DICTIONARY_HAS_BEEN_DELETED("dictionaryHasBeenDeleted"),

    MENU_CURRENT_DICTIONARY("menu_current_dictionary"),
    MENU_SET_DICTIONARY("menu_set_dictionary"),
    MENU_NEW_DICTIONARY("menu_new_dictionary"),
    MENU_MY_1000("menu_my_1000"),
    MENU_FEEDBACK("menu_feedback"),
    MENU_SETTINGS("menu_settings"),
    MENU_HOME("menu_home"),

    INTERFACE_LANGUAGE("interfaceLanguage"),
    CHOOSE_INTERFACE_LANGUAGE("chooseInterfaceLanguage"),

    CANNOT_TRANSLATE_LONG_SENTENCE("cannotTranslateLongSentence"),
    YOU_CAN_USE_TRANSLATE_SENTENCES_NOW("youCanTranslateSentencesNow"),

    NOT_FULL_TRANSLATION("notFullTranslation"),

    CHANGE_MY_LANGUAGE("changeMyLanguage"),
    SET_YOUR_LANGUAGE("setYourLanguage"),

    QUIZ_ADD_WORD_TO_QUIZ("quizAddWordToQuiz"),
    QUIZ_SKIP_WORD("quizSkipWord"),
    QUIZ_DELETE_WORD_FROM_DICTIONARY("quizDeleteWordFromDictionary"),

    QUIZ_TUTORIAL_START_BUTTON("quizTutorialStartButton"),

    QUIZ_TUTORIAL_PART_1("quizTutorialPart1"),

    QUIZ_NEW_WORDS_BUTTON("quizNewWordsButton"),
    QUIZ_REPEAT_WORDS_BUTTON("quizRepeatWordsButton"),
    QUIZ_RANDOM_WORDS_BUTTON("quizRandomWordsButton"),

    QUIZ_TYPE("quizType"),
    QUIZ_CANNOT_BE_STARTED_NO_WORDS("quizCannotBeStartedNoWords"),
    QUIZ_CANNOT_BE_STARTED_NO_WORDS_AND_EMPTY("quizCannotBeStartedNoWordsAndEmpty"),

    START_TUTORIAL("startTutorial"),

    SHOW_LEARNED("showLearned"),
    HIDE_LEARNED("hideLearned"),

    CONTINUE_QUIZ("continueQuiz"),

    ADD_WORD_TO_DICTIONARY("addWordToDictionary"),
    DELETE_FROM_DICTIONARY("deleteFromDictionary"),

    SORTING("sorting"),

    MENU("menu"),
    MENU_BUTTON("menuButton"),
    DICTIONARY_BUTTON("dictionaryButton"),

    HIDE_SOURCE_TEXT("hideSourceText"),
    SHOW_SOURCE_TEXT("showSourceText"),

    COPYING_TEXT_HINT("copyingTextHint"),

    HINT_1("hint1"),
    HINT_2("hint2"),
    HINT_3("hint3"),
    HINT_4("hint4"),
    HINT_5("hint5"),
    HINT_6("hint6"),
    HINT_7("hint7"),

    TURN_ON_NOTIFICATIONS("turnOnNotifications"),
    TURN_OFF_NOTIFICATIONS("turnOffNotifications"),
    TURN_OFF_ALL_NOTIFICATIONS("turnOffAllNotifications"),
    NOTIFICATIONS_HINT("notificationsHint"),
    CHOOSE_NOTIFICATIONS_TIME("chooseNotificationTime"),
    CHOOSE_NOTIFICATIONS_TIME_ZONE("chooseNotificationTimeZone"),
    CHOOSE_NOTIFICATIONS_TIME_ZONE_BY_CITY("chooseNotificationTimeZoneByCity"),

    CLOCK_CURRENT_TIME("clockCurrentTime"),
    CLOCK_7("clock7"),
    CLOCK_8("clock8"),
    CLOCK_9("clock9"),
    CLOCK_10("clock10"),
    CLOCK_11("clock11"),
    CLOCK_12("clock12"),
    CLOCK_13("clock13"),
    CLOCK_14("clock14"),
    CLOCK_15("clock15"),
    CLOCK_16("clock16"),
    CLOCK_17("clock17"),
    CLOCK_18("clock18"),
    CLOCK_19("clock19"),
    CLOCK_20("clock20"),
    CLOCK_21("clock21"),
    CLOCK_22("clock22"),
    CLOCK_23("clock23"),
    CLOCK_00("clock00"),

    TIME_ZONE_0("timeZone0"),
    TIME_ZONE_1("timeZone1"),
    TIME_ZONE_2("timeZone2"),
    TIME_ZONE_3("timeZone3"),
    TIME_ZONE_4("timeZone4"),
    TIME_ZONE_5("timeZone5"),
    TIME_ZONE_6("timeZone6"),
    TIME_ZONE_7("timeZone7"),
    TIME_ZONE_8("timeZone8"),
    TIME_ZONE_9("timeZone9"),
    TIME_ZONE_10("timeZone10"),
    TIME_ZONE_11("timeZone11"),
    TIME_ZONE_12("timeZone12"),

    CHOOSE_NOTIFICATION_TIME_ZONE_MANUALLY("chooseNotificationTimeZoneManually"),
    ALL_NOTIFICATIONS_TURNED_OFF("allNotificationsTurnedOff"),

    YOUR_TIME_ZONE("yourTimeZone"),
    YOU_WILL_BE_NOTIFIED_AT("youWillBeNotifiedAt"),

    ADD_WORDS_FROM_SENTENCE_BY_CLICK("addWordsFromSentenceByClick"),

    WORD_HAS_BEEN_ADDED_TO_DICTIONARY("wordHasBeenAddedToDictionary"),
    WORD_HAS_BEEN_DELETED_FROM_DICTIONARY("wordHasBeenDeletedFromDictionary"),
    WORD_HAS_BEEN_ALREADY_ADDED_TO_DICTIONARY("wordHasBeenAlreadyAddedToDictionary"),
    SHOWING_LEARNED_WORDS("showingLearnedWords"),
    HIDING_LEARNED_WORDS("hidingLearnedWords"),
    ORDER_NEW_TO_OLD("orderNewToOld"),
    ORDER_OLD_TO_NEW("orderOldToNew"),
    TRANSLATION_OF_SENTENCE_WORD_NOT_FOUND("translationOfSentenceWordNotFound"),

    ACTIVITY_TODAY("activityToday"),
    ACTIVITY_WORDS_TRANSLATED("activityWordsTranslated"),
    ACTIVITY_SENTENCES_TRANSLATED("activitySentencesTranslated"),
    ACTIVITY_WORDS_ADDED("activityWordsAdded"),
    ACTIVITY_QUIZZES_PASSED("activityQuizzesPassed"),
    ACTIVITY_WORDS_LEARNED("activityWordsLearned"),

    OXFORD_3000("oxford3000"),

    LEVEL_A1("levelA1"),
    LEVEL_A2("levelA2"),
    LEVEL_B1("levelB1"),
    LEVEL_B2("levelB2"),
    LEVEL_DONT_KNOW("levelDontKnow"),
    INVALID_LEVEL("invalidLevel"),

    DETAILED_TRANSLATION("detailedTranslation"),

    MAIN_MENU_ALERT("mainMenuAlert"),
    SHOW_DICTIONARY_ALERT("showDictionaryAlert"),

    WORD_OF_THE_DAY("wordOfTheDay"),

    SENTENCE_TRANSLATION_UNAVAILABLE("sentenceTranslationUnavailable");

    private final String value;

    public static Text fromValue(String value) {
        for (Text text : Text.values()) {
            if (text.value.equals(value)) {
                return text;
            }
        }
        return null;
    }
}