package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oxford3000LevelCallback extends MessageCallback {
    private String level; // A1
    private Boolean dontKnow; // TRUE

    public Oxford3000LevelCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.OXFORD_3000_LEVEL;
    }

    @Override
    protected String getCallbackData() {
        return level + "_" + dontKnow.toString();
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.level = tokens[1];
        this.dontKnow = Boolean.valueOf(tokens[2]);
    }
}
