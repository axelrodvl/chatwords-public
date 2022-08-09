package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class SetNotificationTimeZoneCallback extends MessageCallback {
    private String zone;

    public SetNotificationTimeZoneCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.SET_NOTIFICATION_TIME_ZONE;
    }

    @Override
    protected String getCallbackData() {
        return zone;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.zone = data;
        } else {
            this.zone = tokens[1];
        }
    }
}
