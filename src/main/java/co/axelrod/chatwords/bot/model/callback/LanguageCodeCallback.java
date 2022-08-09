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
public class LanguageCodeCallback extends MessageCallback {
    private String languageCode;

    public LanguageCodeCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.LANGUAGE_CODE;
    }

    @Override
    protected String getCallbackData() {
        return languageCode;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.languageCode = data;
        } else {
            this.languageCode = tokens[1];
        }
    }
}
