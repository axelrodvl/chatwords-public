package co.axelrod.chatwords.bot.formatter;

import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.storage.Activity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActivityFormatter {
    public static String getFormattedActivity(Activity activity) {
        StringBuilder response = new StringBuilder();
        response.append(UserContext.getText(Text.ACTIVITY_TODAY));
        response.append("\n");
        response.append(UserContext.getText(Text.ACTIVITY_WORDS_ADDED) + " " + activity.getWordsAdded());
        response.append("\n");
        response.append(UserContext.getText(Text.ACTIVITY_QUIZZES_PASSED) + " " + activity.getQuizzesPassed());
        response.append("\n");
        response.append(UserContext.getText(Text.ACTIVITY_WORDS_LEARNED) + " " + activity.getWordsLearned());

        return response.toString();
    }
}
