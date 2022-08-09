package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteWordCallback extends MessageCallback {
    private String dictionaryIdHash;
    private String wordId;

    public DeleteWordCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.REMOVE_WORD;
    }

    @Override
    protected String getCallbackData() {
        return dictionaryIdHash + "_" + wordId;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.dictionaryIdHash = tokens[1];
        this.wordId = tokens[2];
    }
}
