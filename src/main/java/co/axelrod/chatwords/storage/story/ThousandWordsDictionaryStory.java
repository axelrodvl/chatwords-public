package co.axelrod.chatwords.storage.story;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThousandWordsDictionaryStory extends UserStory {
    private Boolean enterLanguage;

    public ThousandWordsDictionaryStory() {
        this.enterLanguage = false;
    }
}
