package co.axelrod.chatwords.storage;

import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.dictionary.oxford.Level;
import co.axelrod.chatwords.storage.paging.Paging;
import co.axelrod.chatwords.storage.story.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Document
@Getter
@Setter
public class User {
    @Id
    private String id;

    private String username;
    private String firstName;
    private String lastName;

    private String languageCode;

    private Language defaultFromLanguage;
    private Language defaultToLanguage;

    private Onboarding onboarding;

    private QuizStory quizStory;
    private StartStory startStory;
    private AlterDictionaryStory alterDictionaryStory;
    private ChangeDictionaryStory changeDictionaryStory;
    private CreateDictionaryStory createDictionaryStory;
    private ThousandWordsDictionaryStory thousandWordsDictionaryStory;
    private Oxford3000DictionaryStory oxford3000DictionaryStory;
    private FeedbackStory feedbackStory;
    private UserSettingsStory userSettingsStory;
    private SentenceToDictionaryStory sentenceToDictionaryStory;

    private Boolean thousandWordsAdded;
    private Boolean oxford3000Added;
    private Level level;

    private Paging paging;

    private UserDictionary currentDictionary;
    private Map<String, UserDictionary> dictionaries;

    private UserSettings userSettings;

    private Boolean userNotifiedAboutUkrainian;
    private Boolean userNotifiedAboutTranslation;

    private String deepLink;

    private String lastTranslation;
    private String lastTranslationResult;
    private Language lastTranslationSourceLanguage;
    private Language lastTranslationTargetLanguage;

    private Boolean botDeleted;

    private Long lastMessageSentTimestamp;
    private Long firstMessageSentInCurrentMinute;
    private Integer messageSentInLastMinute;

    private Boolean showLearned;
    private Boolean showSourceText;

    private Boolean hasMainMenu;

    private Integer currentHint;

    private String timeZoneId;

    private Boolean turnOffWordOfTheDay;

    private Map<String, Activity> activityByDate;

    // For mongo only
    public User() {

    }

    public User(String id, org.telegram.telegrambots.meta.api.objects.User user, String languageCode) {
        this.id = id;
        this.languageCode = languageCode;
        this.dictionaries = new HashMap<>();

        this.username = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();

        this.onboarding = new Onboarding();

        this.userSettings = new UserSettings();

        // Notification only for new users
        this.userNotifiedAboutTranslation = true;

        this.botDeleted = false;

        this.showLearned = true;
    }

    public boolean inStory() {
        return quizStory != null
                || startStory != null
                || alterDictionaryStory != null
                || changeDictionaryStory != null
                || createDictionaryStory != null
                || thousandWordsDictionaryStory != null
                || feedbackStory != null
                || userSettingsStory != null
                || sentenceToDictionaryStory != null
                || oxford3000DictionaryStory != null;
    }

    public void clearStories() {
        this.quizStory = null;
        this.startStory = null;
        this.alterDictionaryStory = null;
        this.changeDictionaryStory = null;
        this.createDictionaryStory = null;
        this.thousandWordsDictionaryStory = null;
        this.feedbackStory = null;
        this.userSettingsStory = null;
        this.sentenceToDictionaryStory = null;
        this.oxford3000DictionaryStory = null;
    }

    public String getName() {
        if (username == null) {
            return id;
        }

        if (firstName == null && lastName == null) {
            return username;
        }

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName
                    + " (" + username + ")";
        }

        return (firstName == null ? lastName : firstName)
                + " (" + username + ")";
    }

    public Activity getCurrentDayActivity() {
        String currentDate = LocalDate.now().toString();

        if (getActivityByDate() == null) {
            activityByDate = new HashMap<>();
        }

        Activity currentDayActivity = UserContext.getUser().getActivityByDate().get(currentDate);
        if (currentDayActivity == null) {
            currentDayActivity = new Activity();
            activityByDate.put(currentDate, currentDayActivity);
        }

        return currentDayActivity;
    }
}
