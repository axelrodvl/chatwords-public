package co.axelrod.chatwords.bot.model.callback;

import co.axelrod.chatwords.bot.command.Callback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageCallback {
    protected Callback callback;

    public abstract Callback getCallbackName();

    protected abstract String getCallbackData();

    public String code() {
        String code = getCallbackName().getId() + "_" + getCallbackData();

        if (code.length() > 64) {
            log.error("Too long callback data: " + code);
        }

        return getCallbackName().getId() + "_" + getCallbackData();
    }

    public abstract void decode(String data);
}
