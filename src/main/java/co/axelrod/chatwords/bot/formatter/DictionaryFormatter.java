package co.axelrod.chatwords.bot.formatter;

import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.storage.UserDictionary;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DictionaryFormatter {
    public static String currentDictionaryName() {
        Language sourceLanguage = UserContext.getUser().getCurrentDictionary().getSourceLanguage();
        Language targetLanguage = UserContext.getUser().getCurrentDictionary().getTargetLanguage();

        StringBuilder response = new StringBuilder();
        response.append("<b>");
        response.append(UserContext.getUser().getCurrentDictionary().getName());
        response.append("</b> ");
        response.append(sourceLanguage.getEmoji());
        response.append(targetLanguage.getEmoji());

        return response.toString();
    }

    public static String getDictionary(UserDictionary userDictionary) {
        StringBuilder response = new StringBuilder();
        response.append(currentDictionaryName());
        response.append("\n");

        if (!userDictionary.getWords().isEmpty()) {
            response.append("<i>");

            response.append(UserContext.getText(Text.KNOWN_WORDS));
            response.append(": ");
            response.append(userDictionary.getLearnedWordsCount());
            response.append("/");
            response.append(userDictionary.getWords().size());
            response.append(" (");
            response.append(userDictionary.getLearnedPercent());
            response.append(")");

            response.append("</i>");
            response.append("\n");
        }

        return response.toString();
    }
}
