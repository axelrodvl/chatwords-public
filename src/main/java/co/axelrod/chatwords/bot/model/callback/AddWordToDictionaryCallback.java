package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddWordToDictionaryCallback extends MessageCallback {
    private String wordId; // 610e66a202700c2351d07d7b
    private String source; // ru
    private String target; // en

    public AddWordToDictionaryCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.ADD_WORD_TO_DICTIONARY;
    }

    @Override
    protected String getCallbackData() {
        return wordId + "_" + source + "_" + target;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.wordId = tokens[1];
        this.source = tokens[2];
        this.target = tokens[3];
    }
}