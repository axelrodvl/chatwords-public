package co.axelrod.chatwords.bot.util;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class CallbackUtil {
    public static Callback getCallback(String data) {
        if (data == null || data.length() == 0 || !data.contains("_")) {
            return null;
        }

        String id = data.split("_")[0];

        return Arrays.stream(Callback.values())
                .filter(c -> c.getId().equals(id))
                .findFirst().orElse(null);
    }
}
