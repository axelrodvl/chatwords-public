package co.axelrod.chatwords.storage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettings {
    private String languageCode;
    private Integer quizLength;
}
