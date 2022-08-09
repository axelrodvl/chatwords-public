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
public class SetQuizLengthCallback extends MessageCallback {
    private String length;

    public SetQuizLengthCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.SET_QUIZ_LENGTH;
    }

    @Override
    protected String getCallbackData() {
        return length;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.length = data;
        } else {
            this.length = tokens[1];
        }
    }
}
