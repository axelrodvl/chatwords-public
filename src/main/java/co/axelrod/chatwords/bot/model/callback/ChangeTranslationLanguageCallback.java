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
public class ChangeTranslationLanguageCallback extends MessageCallback {
    private String targetLanguageId;

    public ChangeTranslationLanguageCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.CHANGE_TRANSLATION_LANGUAGE;
    }

    @Override
    protected String getCallbackData() {
        return targetLanguageId;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.targetLanguageId = data;
        } else {
            this.targetLanguageId = tokens[1];
        }
    }
}
