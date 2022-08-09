package co.axelrod.chatwords.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {
    START("/start", "NO_DESCRIPTION"),

    CHANGE_DICTIONARY("/set_dictionary", "menu_set_dictionary"),
    NEW_DICTIONARY("/new_dictionary", "menu_new_dictionary"),
    MY_1000("/my_1000", "menu_my_1000"),
    OXFORD_3000("/oxford_3000", "menu_oxford_3000"),
    FEEDBACK("/feedback", "menu_feedback"),
    SETTINGS("/settings", "menu_settings"),
    HOME("/home", "menu_home"),

    UNKNOWN("UNKNOWN", "NO_DESCRIPTION");

    private final String value;
    private final String description;

    public static Command getCommand(String name) {
        for (Command command : values()) {
            if (command.value.equals(name)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
