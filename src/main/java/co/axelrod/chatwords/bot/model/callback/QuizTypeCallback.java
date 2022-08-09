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
public class QuizTypeCallback extends MessageCallback {
    private String type;

    public QuizTypeCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.QUIZ_TYPE;
    }

    @Override
    protected String getCallbackData() {
        return type;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.type = data;
        } else {
            this.type = tokens[1];
        }
    }
}
