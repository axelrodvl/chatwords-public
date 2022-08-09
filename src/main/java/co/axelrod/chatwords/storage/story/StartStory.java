package co.axelrod.chatwords.storage.story;

import co.axelrod.chatwords.dictionary.model.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartStory extends UserStory {
    private Language defaultToLanguage;

    public StartStory() {
        this.defaultToLanguage = null;
    }
}
