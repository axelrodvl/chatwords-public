package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetNotificationTimeCallback extends MessageCallback {
    private String dictionaryIdHash;
    private String time;

    public SetNotificationTimeCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.SET_NOTIFICATION_TIME;
    }

    @Override
    protected String getCallbackData() {
        return dictionaryIdHash + "_" + time;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.dictionaryIdHash = tokens[1];
        this.time = tokens[2];
    }
}
