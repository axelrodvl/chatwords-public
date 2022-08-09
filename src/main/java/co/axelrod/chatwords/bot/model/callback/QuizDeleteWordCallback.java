package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDeleteWordCallback extends MessageCallback {
    private String dictionaryId;
    private String wordId;

    public QuizDeleteWordCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.QUIZ_REMOVE_WORD;
    }

    @Override
    protected String getCallbackData() {
        return dictionaryId + "_" + wordId;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.dictionaryId = tokens[1];
        this.wordId = tokens[2];
    }
}
