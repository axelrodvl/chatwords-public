package co.axelrod.chatwords.service.model;

import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import lombok.Getter;

@Getter
public class ServiceResponse {
    private String message;
    private Text text;
    private UserEvent userEvent;

    public ServiceResponse(String message, UserEvent userEvent) {
        this.message = message;
        this.userEvent = userEvent;
    }

    public ServiceResponse(Text text, UserEvent userEvent) {
        this.text = text;
        this.userEvent = userEvent;
    }

    public ServiceResponse() {

    }

    public boolean isSuccess() {
        return message == null && text == null && userEvent == null;
    }
}
