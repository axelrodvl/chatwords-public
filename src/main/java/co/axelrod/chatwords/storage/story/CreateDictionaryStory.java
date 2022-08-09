package co.axelrod.chatwords.storage.story;

import co.axelrod.chatwords.dictionary.model.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDictionaryStory extends UserStory {
    private Language targetLanguage;

    private Boolean enterName;
    private Boolean enterLanguage;

    public CreateDictionaryStory() {
        this.targetLanguage = null;
        this.enterName = false;
        this.enterLanguage = false;
    }
}
