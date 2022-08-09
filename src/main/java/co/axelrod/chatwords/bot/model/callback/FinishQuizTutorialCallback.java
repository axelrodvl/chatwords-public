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
public class FinishQuizTutorialCallback extends MessageCallback {
    public FinishQuizTutorialCallback(String data) {
        decode(data);
    }

    @Override
    public Callback getCallbackName() {
        return Callback.FINISH_QUIZ_TUTORIAL;
    }

    @Override
    protected String getCallbackData() {
        return null;
    }

    @Override
    public void decode(String data) {

    }
}
