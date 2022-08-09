package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordActionCallback extends MessageCallback {
    private String type;
    private String wordId;

    public WordActionCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.QUIZ_WORD_ACTION;
    }

    @Override
    protected String getCallbackData() {
        return type + "_" + wordId;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.type = tokens[1];
        this.wordId = tokens[2];
    }
}
