package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedWordTranslationCallback extends MessageCallback {
    private String wordId; // 610e66a202700c2351d07d7b
    private String languageFrom; // ru
    private String languageTo; // en

    public DetailedWordTranslationCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.DETAILED_WORD_TRANSLATION;
    }

    @Override
    protected String getCallbackData() {
        return wordId + "_" + languageFrom + "_" + languageTo;
    }

    @Override
    public void decode(String data) {
        String[] tokens = data.split("_");
        this.wordId = tokens[1];
        this.languageFrom = tokens[2];
        this.languageTo = tokens[3];
    }
}
