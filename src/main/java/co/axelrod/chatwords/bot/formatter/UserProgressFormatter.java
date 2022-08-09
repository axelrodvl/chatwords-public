package co.axelrod.chatwords.bot.formatter;

import co.axelrod.chatwords.bot.i18n.Text;
import co.axelrod.chatwords.bot.model.UserContext;
import co.axelrod.chatwords.service.model.UserProgress;
import co.axelrod.chatwords.storage.UserDictionary;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserProgressFormatter {
    public static String format(UserProgress userProgress) {
        StringBuilder response = new StringBuilder();

//        response.append("\uD83D\uDCC8 Текущая неделя\n" +
//                "\n" +
//                "\uD83D\uDDD3️ Активные дни:\n" +
//                "<b>Пн <s>Вт</s> Ср <s>Чт</s> Пт Сб <s>Вс</s></b>\n" +
//                "✅ ❌ ✅ ✅ ✅ ❌ ❌"
//                + "\n\n");

        response.append(UserContext.getText(Text.MY_WORDS));
        response.append("\n");
        response.append(userProgress.getLearnedWordsCount());
        response.append("/");
        response.append(userProgress.getWordsCount());
        response.append(" (");
        response.append(UserDictionary.formatLearnedPercent(userProgress.getTotalScore()));
        response.append(")");

//        response.append("\n\n");
//        response.append(UserContext.getText(Text.CURRENT_DICTIONARY));
//        response.append("\n");
//        response.append(userProgress.getCurrentDictionary().getName());
//        response.append(" ");
//        response.append(userProgress.getCurrentDictionary().getSourceLanguage().getEmoji());
//        response.append(userProgress.getCurrentDictionary().getTargetLanguage().getEmoji());

        return response.toString();
    }
}
