package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeWordTranslationCallback extends MessageCallback {
    private String wordId; // 610e66a202700c2351d07d7b
    private String translationHash; // 0123456789
    private String languageFrom; // ru
    private String languageTo; // en

    public ChangeWordTranslationCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.CHANGE_WORD_TRANSLATION_VARIANT;
    }

    @Override
    protected String getCallbackData() {
        return wordId + "_" + translationHash + "_" + languageFrom + "_" + languageTo;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.wordId = tokens[1];
        this.translationHash = tokens[2];
        this.languageFrom = tokens[3];
        this.languageTo = tokens[4];
    }
}
