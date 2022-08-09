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
public class ChangeTranslationModeCallback extends MessageCallback {
    private String showSourceText; // TRUE

    public ChangeTranslationModeCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.CHANGE_TRANSLATION_MODE;
    }

    @Override
    protected String getCallbackData() {
        return showSourceText;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");

        if (tokens.length == 1) {
            this.showSourceText = data;
        } else {
            this.showSourceText = tokens[1];
        }
    }
}
