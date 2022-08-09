package co.axelrod.chatwords.validator.model;

import co.axelrod.chatwords.bot.analytics.UserEvent;
import co.axelrod.chatwords.bot.i18n.Text;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class ValidationResult {
    private final boolean valid;
    private Text text;
    private UserEvent userEvent;
}
